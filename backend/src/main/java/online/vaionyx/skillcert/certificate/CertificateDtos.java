package online.vaionyx.skillcert.certificate;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class CertificateDtos {
  private CertificateDtos() {}

  public record IssueCertificateRequest(@NotNull UUID attemptId) {}

  public record CertificateDetails(
      String shortId,
      String recipientName,
      String certificationSlug,
      String courseName,
      int score,
      int totalQuestions,
      Instant issuedAt,
      String status,
      String verificationUrl,
      String pngUrl,
      String pdfUrl) {}
}
