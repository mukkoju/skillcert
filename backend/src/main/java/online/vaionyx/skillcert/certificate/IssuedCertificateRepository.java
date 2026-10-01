package online.vaionyx.skillcert.certificate;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuedCertificateRepository extends JpaRepository<IssuedCertificate, UUID> {
  Optional<IssuedCertificate> findByShortId(String shortId);
  Optional<IssuedCertificate> findByAttemptId(UUID attemptId);
  boolean existsByShortId(String shortId);
}
