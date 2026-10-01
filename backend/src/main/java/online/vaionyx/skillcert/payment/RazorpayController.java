package online.vaionyx.skillcert.payment;

import jakarta.validation.Valid;
import online.vaionyx.skillcert.certificate.CertificateDtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/razorpay")
public class RazorpayController {
  private final RazorpayPaymentService payments;
  public RazorpayController(RazorpayPaymentService payments) { this.payments = payments; }

  @PostMapping("/orders")
  public ResponseEntity<PaymentDtos.CheckoutOrder> order(@Valid @RequestBody PaymentDtos.CreateOrderRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(payments.createOrder(request.attemptId()));
  }
  @PostMapping("/verify")
  public CertificateDtos.CertificateDetails verify(@Valid @RequestBody PaymentDtos.VerifyPaymentRequest request) { return payments.verifyCheckout(request); }
  @PostMapping("/webhook")
  public ResponseEntity<Void> webhook(@RequestBody String body, @RequestHeader("X-Razorpay-Signature") String signature) { payments.receiveWebhook(body, signature); return ResponseEntity.ok().build(); }
}
