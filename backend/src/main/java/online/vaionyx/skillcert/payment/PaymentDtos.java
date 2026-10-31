package online.vaionyx.skillcert.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class PaymentDtos {
  private PaymentDtos() {}
  public record CheckoutOrder(@NotNull UUID attemptId, @NotBlank String razorpayOrderId, int amountPaise, String currency, String keyId, String recipientName, String recipientEmail, String recipientMobile, String courseName) {}
  public record CreateOrderRequest(@NotNull UUID attemptId, CertificatePackage packageType, @Size(max = 1000) String shippingAddress, @Size(max = 7) List<@Size(max = 120) String> certificationSlugs) {}
  public record VerifyPaymentRequest(@NotNull UUID attemptId, @NotBlank String razorpayPaymentId, @NotBlank String razorpayOrderId, @NotBlank String razorpaySignature) {}
}
