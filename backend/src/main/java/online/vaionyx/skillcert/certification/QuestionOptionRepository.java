package online.vaionyx.skillcert.certification;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> { List<QuestionOption> findByQuestionIdOrderByPositionAsc(UUID questionId); Optional<QuestionOption> findByIdAndQuestionId(UUID id, UUID questionId); }
