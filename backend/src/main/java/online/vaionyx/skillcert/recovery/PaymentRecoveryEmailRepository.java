package online.vaionyx.skillcert.recovery;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecoveryEmailRepository extends JpaRepository<PaymentRecoveryEmail, UUID> {
  Optional<PaymentRecoveryEmail> findByAttemptIdAndStage(UUID attemptId, String stage);
  Optional<PaymentRecoveryEmail> findByResumeToken(String resumeToken);
}
