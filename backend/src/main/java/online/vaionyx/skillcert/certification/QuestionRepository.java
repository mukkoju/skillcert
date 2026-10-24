package online.vaionyx.skillcert.certification;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
  List<Question> findByCertificationIdAndPublishedTrueOrderByPositionAsc(UUID certificationId);

  List<Question> findByIdInAndCertificationIdAndPublishedTrue(Collection<UUID> ids, UUID certificationId);

  @Query(value = "select * from questions where certification_id = :certificationId and published = true and difficulty = :difficulty order by random() limit :limit", nativeQuery = true)
  List<Question> pickRandomByDifficulty(@Param("certificationId") UUID certificationId, @Param("difficulty") String difficulty, @Param("limit") int limit);
}
