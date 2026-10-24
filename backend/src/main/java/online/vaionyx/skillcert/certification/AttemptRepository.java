package online.vaionyx.skillcert.certification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
  @Query("""
      select a from Attempt a
      where a.passed = true
        and a.recipientEmail is not null
        and a.recipientEmail <> ''
        and a.createdAt >= :recoveryStartedAt
        and a.createdAt <= :completedBefore
        and not exists (
          select p from Payment p where p.attempt = a and p.status = 'CAPTURED'
        )
      order by a.createdAt asc
      """)
  List<Attempt> findUnpaidCompletedBefore(
      @Param("recoveryStartedAt") Instant recoveryStartedAt,
      @Param("completedBefore") Instant completedBefore);
}
