package online.vaionyx.skillcert.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import online.vaionyx.skillcert.certificate.CertificateDtos;
import online.vaionyx.skillcert.certificate.CertificateIssuanceService;
import online.vaionyx.skillcert.certificate.CertificateEmailService;
import online.vaionyx.skillcert.certification.Attempt;
import online.vaionyx.skillcert.certification.AttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RazorpayPaymentService {
  private static final String API = "https://api.razorpay.com/v1";
  private final AttemptRepository attempts;
  private final PaymentRepository payments;
  private final CertificateIssuanceService issuance;
  private final CertificateEmailService email;
  private final ObjectMapper json;
  private final HttpClient http = HttpClient.newHttpClient();
  private final String keyId;
  private final String keySecret;
  private final String webhookSecret;

  public RazorpayPaymentService(AttemptRepository attempts, PaymentRepository payments, CertificateIssuanceService issuance, CertificateEmailService email, ObjectMapper json,
      @Value("${app.razorpay-key-id:}") String keyId,
      @Value("${app.razorpay-key-secret:}") String keySecret,
      @Value("${app.razorpay-webhook-secret:}") String webhookSecret) {
    this.attempts = attempts; this.payments = payments; this.issuance = issuance; this.email = email; this.json = json;
    this.keyId = keyId; this.keySecret = keySecret; this.webhookSecret = webhookSecret;
  }

  @Transactional
  public PaymentDtos.CheckoutOrder createOrder(UUID attemptId) {
    requireKeys();
    Attempt attempt = attempts.findById(attemptId).orElseThrow(() -> notFound("Assessment attempt not found"));
    if (!attempt.isPassed()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only a passed assessment can be paid for");
    Payment existing = payments.findByAttemptId(attemptId).orElse(null);
    if (existing != null) {
      if ("CAPTURED".equals(existing.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Credential has already been issued");
      return checkout(existing);
    }
    int amount = attempt.getCertification().getPricePaise();
    JsonNode response = razorpay("POST", "/orders", "{\"amount\":" + amount + ",\"currency\":\"INR\",\"receipt\":\"attempt_" + attempt.getId() + "\",\"notes\":{\"attempt_id\":\"" + attempt.getId() + "\"}}");
    Payment payment = payments.save(new Payment(attempt, amount, response.path("id").asText()));
    return checkout(payment);
  }

  @Transactional
  public CertificateDtos.CertificateDetails verifyCheckout(PaymentDtos.VerifyPaymentRequest request) {
    requireKeys();
    Payment payment = payments.findByAttemptId(request.attemptId()).orElseThrow(() -> notFound("Payment order not found"));
    if (!payment.getRazorpayOrderId().equals(request.razorpayOrderId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment order mismatch");
    if (!constantTimeEquals(hmac(payment.getRazorpayOrderId() + "|" + request.razorpayPaymentId(), keySecret), request.razorpaySignature())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment signature");
    }
    JsonNode remotePayment = razorpay("GET", "/payments/" + request.razorpayPaymentId(), null);
    if (!"captured".equals(remotePayment.path("status").asText())) {
      throw new ResponseStatusException(HttpStatus.ACCEPTED, "Payment is awaiting capture");
    }
    payment.markCaptured(request.razorpayPaymentId());
    CertificateDtos.CertificateDetails certificate = issuance.issueAfterPayment(request.attemptId());
    email.deliver(certificate.shortId());
    return certificate;
  }

  @Transactional
  public void receiveWebhook(String rawPayload, String signature) {
    if (webhookSecret.isBlank() || !constantTimeEquals(hmac(rawPayload, webhookSecret), signature)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook signature");
    }
    try {
      JsonNode root = json.readTree(rawPayload);
      if (!"payment.captured".equals(root.path("event").asText())) return;
      JsonNode entity = root.path("payload").path("payment").path("entity");
      Payment payment = payments.findByRazorpayOrderId(entity.path("order_id").asText()).orElse(null);
      if (payment == null || "CAPTURED".equals(payment.getStatus())) return;
      payment.markCaptured(entity.path("id").asText());
      CertificateDtos.CertificateDetails certificate = issuance.issueAfterPayment(payment.getAttempt().getId());
      email.deliver(certificate.shortId());
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook payload");
    }
  }

  private PaymentDtos.CheckoutOrder checkout(Payment payment) {
    Attempt attempt = payment.getAttempt();
    return new PaymentDtos.CheckoutOrder(attempt.getId(), payment.getRazorpayOrderId(), payment.getAmountPaise(), "INR", keyId, attempt.getRecipientName(), attempt.getCertification().getTitle());
  }

  private JsonNode razorpay(String method, String path, String body) {
    try {
      HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API + path)).header("Authorization", "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8)));
      if ("POST".equals(method)) request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
      else request.GET();
      HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Razorpay could not process the request");
      return json.readTree(response.body());
    } catch (ResponseStatusException exception) { throw exception;
    } catch (Exception exception) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to reach Razorpay"); }
  }

  private void requireKeys() { if (keyId.isBlank() || keySecret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Razorpay is not configured"); }
  private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
  private String hmac(String value, String secret) {
    try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); return java.util.HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8))); }
    catch (Exception exception) { throw new IllegalStateException("Unable to sign Razorpay payload", exception); }
  }
  private boolean constantTimeEquals(String expected, String actual) { return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8)); }
}
