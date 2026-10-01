package online.vaionyx.skillcert.certification;
import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface AttemptRepository extends JpaRepository<Attempt, UUID> {}
