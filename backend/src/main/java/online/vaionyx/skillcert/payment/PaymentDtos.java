package online.vaionyx.skillcert.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class PaymentDtos {
  private PaymentDtos() {}
  public record CheckoutOrder(@NotNull UUID attemptId, @NotBlank String razorpayOrderId, int amountPaise, String currency, String keyId, String recipientName, String courseName) {}
  public record CreateOrderRequest(@NotNull UUID attemptId) {}
  public record VerifyPaymentRequest(@NotNull UUID attemptId, @NotBlank String razorpayPaymentId, @NotBlank String razorpayOrderId, @NotBlank String razorpaySignature) {}
}
