package online.vaionyx.skillcert.recovery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payment_recovery_settings")
public class PaymentRecoverySettings {
  @Id private Short id;
  @Column(name = "activated_at", nullable = false) private Instant activatedAt;

  protected PaymentRecoverySettings() {}

  public PaymentRecoverySettings(Instant activatedAt) {
    this.id = 1;
    this.activatedAt = activatedAt;
  }

  public Instant getActivatedAt() { return activatedAt; }
}
