package online.vaionyx.skillcert.certification;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface QuestionRepository extends JpaRepository<Question, UUID> { List<Question> findByCertificationIdAndPublishedTrueOrderByPositionAsc(UUID certificationId); }
