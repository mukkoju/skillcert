package online.vaionyx.skillcert.certification;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface CertificationRepository extends JpaRepository<Certification, UUID> { List<Certification> findByPublishedTrueOrderByTitleAsc(); Optional<Certification> findBySlugAndPublishedTrue(String slug); }
