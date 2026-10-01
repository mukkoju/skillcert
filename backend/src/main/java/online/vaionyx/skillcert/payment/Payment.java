package online.vaionyx.skillcert.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import online.vaionyx.skillcert.certification.Attempt;

@Entity
@Table(name = "payments")
public class Payment {
  @Id @GeneratedValue private UUID id;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "attempt_id", nullable = false, unique = true) private Attempt attempt;
  @Column(name = "amount_paise", nullable = false) private int amountPaise;
  @Column(nullable = false) private String currency;
  @Column(name = "razorpay_order_id", nullable = false, unique = true) private String razorpayOrderId;
  @Column(name = "razorpay_payment_id", unique = true) private String razorpayPaymentId;
  @Column(nullable = false) private String status;
  @Column(name = "created_at", nullable = false) private Instant createdAt;
  @Column(name = "paid_at") private Instant paidAt;

  protected Payment() {}
  public Payment(Attempt attempt, int amountPaise, String razorpayOrderId) {
    this.attempt = attempt; this.amountPaise = amountPaise; this.currency = "INR";
    this.razorpayOrderId = razorpayOrderId; this.status = "CREATED"; this.createdAt = Instant.now();
  }
  public Attempt getAttempt(){return attempt;} public int getAmountPaise(){return amountPaise;} public String getRazorpayOrderId(){return razorpayOrderId;} public String getRazorpayPaymentId(){return razorpayPaymentId;} public String getStatus(){return status;}
  public void markCaptured(String paymentId) { this.razorpayPaymentId = paymentId; this.status = "CAPTURED"; this.paidAt = Instant.now(); }
  public void markFailed() { this.status = "FAILED"; }
}
