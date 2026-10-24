package online.vaionyx.skillcert.recovery;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecoveryEmailRepository extends JpaRepository<PaymentRecoveryEmail, UUID> {
  @EntityGraph(attributePaths = {"attempt", "attempt.certification"})
  Optional<PaymentRecoveryEmail> findByAttemptIdAndStage(UUID attemptId, String stage);
  Optional<PaymentRecoveryEmail> findByResumeToken(String resumeToken);
}
