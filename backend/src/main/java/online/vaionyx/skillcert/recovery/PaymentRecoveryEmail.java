package online.vaionyx.skillcert.recovery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import online.vaionyx.skillcert.certification.Attempt;

@Entity
@Table(name = "payment_recovery_emails")
public class PaymentRecoveryEmail {
  @Id @GeneratedValue private UUID id;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "attempt_id", nullable = false) private Attempt attempt;
  @Column(nullable = false) private String stage;
  @Column(name = "resume_token", nullable = false, unique = true) private String resumeToken;
  @Column(name = "sent_at") private Instant sentAt;
  @Column(name = "delivery_attempts", nullable = false) private int deliveryAttempts;
  @Column(name = "failed_at") private Instant failedAt;
  @Column(name = "created_at", nullable = false) private Instant createdAt;

  protected PaymentRecoveryEmail() {}

  public PaymentRecoveryEmail(Attempt attempt, String stage, String resumeToken) {
    this.attempt = attempt;
    this.stage = stage;
    this.resumeToken = resumeToken;
    this.createdAt = Instant.now();
  }

  public Attempt getAttempt() { return attempt; }
  public String getStage() { return stage; }
  public String getResumeToken() { return resumeToken; }
  public Instant getSentAt() { return sentAt; }
  public int getDeliveryAttempts() { return deliveryAttempts; }
  public boolean hasReachedDeliveryLimit() { return deliveryAttempts >= 3; }
  public boolean beginDeliveryAttempt() {
    if (hasReachedDeliveryLimit()) return false;
    deliveryAttempts++;
    return true;
  }
  public void markSent() { this.sentAt = Instant.now(); }
  public void markFailed() { this.failedAt = Instant.now(); }
}
