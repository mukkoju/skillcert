package online.vaionyx.skillcert.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CertificateEmailService {
  private static final URI RESEND_EMAILS = URI.create("https://api.resend.com/emails");
  private final IssuedCertificateRepository certificates;
  private final CertificateRenderService renderer;
  private final ObjectMapper json;
  private final String apiKey;
  private final String fromEmail;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  public CertificateEmailService(
      IssuedCertificateRepository certificates,
      CertificateRenderService renderer,
      ObjectMapper json,
      @Value("${app.resend-api-key:}") String apiKey,
      @Value("${app.certificate-from-email:}") String fromEmail) {
    this.certificates = certificates;
    this.renderer = renderer;
    this.json = json;
    this.apiKey = apiKey;
    this.fromEmail = fromEmail;
  }

  @Transactional
  public void deliver(String shortId) {
    IssuedCertificate certificate = certificates.findWithLockByShortId(shortId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
    if (certificate.getEmailSentAt() != null) return;
    if (certificate.getAttempt().getRecipientEmail() == null || certificate.getAttempt().getRecipientEmail().isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Certificate email address is required");
    }
    if (apiKey.isBlank() || fromEmail.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Certificate email is not configured");
    }

    CertificateRequest request = new CertificateRequest(
        certificate.getRecipientName(), certificate.getCertification().getTitle(), certificate.getShortId(),
        certificate.getIssuedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate(),
        "https://skillcert.vaionyxsolutions.online/verify/" + certificate.getShortId());
    byte[] pdf = renderer.renderPdf(request);
    send(certificate, pdf, request.verificationUrl());
    certificate.markEmailSent();
  }

  private void send(IssuedCertificate certificate, byte[] pdf, String verificationUrl) {
    try {
      ObjectNode payload = json.createObjectNode();
      payload.put("from", fromEmail);
      ArrayNode recipients = payload.putArray("to");
      recipients.add(certificate.getAttempt().getRecipientEmail());
      payload.put("subject", "Your " + certificate.getCertification().getTitle() + " certificate is ready");
      payload.put("html", html(certificate, verificationUrl));
      ObjectNode attachment = payload.putArray("attachments").addObject();
      attachment.put("filename", "SkillCert-" + certificate.getShortId() + ".pdf");
      attachment.put("content", Base64.getEncoder().encodeToString(pdf));
      attachment.put("content_type", "application/pdf");

      HttpRequest httpRequest = HttpRequest.newBuilder(RESEND_EMAILS)
          .timeout(Duration.ofSeconds(30))
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .header("Idempotency-Key", "skillcert-certificate/" + certificate.getShortId())
          .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
          .build();
      HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Certificate email could not be delivered");
      }
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Certificate email could not be delivered");
    }
  }

  private String html(IssuedCertificate certificate, String verificationUrl) {
    String recipient = escape(certificate.getRecipientName());
    String course = escape(certificate.getCertification().getTitle());
    String safeUrl = escape(verificationUrl);
    return "<div style=\"font-family:Arial,sans-serif;color:#10213b;line-height:1.55\">"
        + "<h1>Your SkillCert certificate is ready</h1>"
        + "<p>Hi " + recipient + ",</p>"
        + "<p>Your verified <strong>" + course + "</strong> certificate is attached as a PDF.</p>"
        + "<p><a href=\"" + safeUrl + "\">Verify your certificate online</a></p>"
        + "<p>Certificate ID: " + escape(certificate.getShortId()) + "</p>"
        + "<p>— SkillCert by Vaionyx</p></div>";
  }

  private String escape(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
}
