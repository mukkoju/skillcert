package online.vaionyx.skillcert.instagram;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "instagram_publications")
public class InstagramPublication {
  @Id private UUID id;
  @Column(name = "publication_date", nullable = false, unique = true) private LocalDate publicationDate;
  @Column(name = "container_id") private String containerId;
  @Column(name = "instagram_media_id") private String instagramMediaId;
  @Column(nullable = false) private String status;
  @Column(name = "created_at", nullable = false) private Instant createdAt;
  @Column(name = "published_at") private Instant publishedAt;

  protected InstagramPublication() {}

  public InstagramPublication(LocalDate publicationDate) {
    this.id = UUID.randomUUID();
    this.publicationDate = publicationDate;
    this.status = "CREATED";
    this.createdAt = Instant.now();
  }

  public void markPublished(String containerId, String instagramMediaId) {
    this.containerId = containerId;
    this.instagramMediaId = instagramMediaId;
    this.status = "PUBLISHED";
    this.publishedAt = Instant.now();
  }
}
