package online.vaionyx.skillcert.certification;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptAuditRepository extends JpaRepository<AttemptAudit, UUID> {
  boolean existsByAttemptId(UUID attemptId);
}
