package online.vaionyx.skillcert.instagram;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface InstagramPublicationRepository extends JpaRepository<InstagramPublication, UUID> {
  boolean existsByPublicationDate(LocalDate publicationDate);
}
