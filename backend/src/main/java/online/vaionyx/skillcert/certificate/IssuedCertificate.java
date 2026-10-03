package online.vaionyx.skillcert.certificate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import online.vaionyx.skillcert.certification.Attempt;
import online.vaionyx.skillcert.certification.Certification;

@Entity
@Table(name = "issued_certificates")
public class IssuedCertificate {
  @Id @GeneratedValue private UUID id;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "attempt_id", nullable = false, unique = true) private Attempt attempt;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "certification_id", nullable = false) private Certification certification;
  @Column(name = "recipient_name", nullable = false) private String recipientName;
  @Column(name = "short_id", nullable = false, unique = true) private String shortId;
  @Column(nullable = false) private int score;
  @Column(name = "total_questions", nullable = false) private int totalQuestions;
  @Column(nullable = false) private String status;
  @Column(name = "issued_at", nullable = false) private Instant issuedAt;
  @Column(name = "email_sent_at") private Instant emailSentAt;
  @Column(name = "png_object_key") private String pngObjectKey;
  @Column(name = "pdf_object_key") private String pdfObjectKey;

  protected IssuedCertificate() {}

  public IssuedCertificate(Attempt attempt, String shortId) {
    this.attempt = attempt;
    this.certification = attempt.getCertification();
    this.recipientName = attempt.getRecipientName();
    this.shortId = shortId;
    this.score = attempt.getScore();
    this.totalQuestions = attempt.getTotalQuestions();
    this.status = "ISSUED";
    this.issuedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public Attempt getAttempt() { return attempt; }
  public Certification getCertification() { return certification; }
  public String getRecipientName() { return recipientName; }
  public String getShortId() { return shortId; }
  public int getScore() { return score; }
  public int getTotalQuestions() { return totalQuestions; }
  public String getStatus() { return status; }
  public Instant getIssuedAt() { return issuedAt; }
  public Instant getEmailSentAt() { return emailSentAt; }
  public String getPngObjectKey() { return pngObjectKey; }
  public String getPdfObjectKey() { return pdfObjectKey; }
  public void markEmailSent() { this.emailSentAt = Instant.now(); }
  public void markStored(String pngObjectKey, String pdfObjectKey) { this.pngObjectKey = pngObjectKey; this.pdfObjectKey = pdfObjectKey; }
}
