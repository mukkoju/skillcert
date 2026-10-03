package online.vaionyx.skillcert.certificate;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface IssuedCertificateRepository extends JpaRepository<IssuedCertificate, UUID> {
  Optional<IssuedCertificate> findByShortId(String shortId);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select certificate from IssuedCertificate certificate where certificate.shortId = :shortId")
  Optional<IssuedCertificate> findWithLockByShortId(@Param("shortId") String shortId);
  Optional<IssuedCertificate> findByAttemptId(UUID attemptId);
  boolean existsByShortId(String shortId);
}
