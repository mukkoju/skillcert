package online.vaionyx.skillcert.certificate;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import online.vaionyx.skillcert.certification.Attempt;
import online.vaionyx.skillcert.certification.AttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CertificateIssuanceService {
  private static final char[] SHORT_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
  private final AttemptRepository attempts;
  private final IssuedCertificateRepository certificates;
  private final SecureRandom random = new SecureRandom();
  private final String verificationBaseUrl;
  private final String r2PublicBaseUrl;

  public CertificateIssuanceService(
      AttemptRepository attempts,
      IssuedCertificateRepository certificates,
      @Value("${app.certificate-verification-base-url:https://skillcert.vaionyxsolutions.online/verify/}") String verificationBaseUrl,
      @Value("${app.r2-public-base-url:}") String r2PublicBaseUrl) {
    this.attempts = attempts;
    this.certificates = certificates;
    this.verificationBaseUrl = verificationBaseUrl.endsWith("/") ? verificationBaseUrl : verificationBaseUrl + "/";
    this.r2PublicBaseUrl = r2PublicBaseUrl.endsWith("/") ? r2PublicBaseUrl.substring(0, r2PublicBaseUrl.length() - 1) : r2PublicBaseUrl;
  }

  @Transactional
  public CertificateDtos.CertificateDetails issueAfterPayment(UUID attemptId) {
    IssuedCertificate existing = certificates.findByAttemptId(attemptId).orElse(null);
    if (existing != null) return details(existing);

    Attempt attempt = attempts.findById(attemptId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment attempt not found"));
    if (!attempt.isPassed()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only a passed assessment can receive a certificate");
    }
    return details(certificates.save(new IssuedCertificate(attempt, nextShortId())));
  }

  @Transactional(readOnly = true)
  public CertificateDtos.CertificateDetails find(String shortId) {
    return details(load(shortId));
  }

  @Transactional(readOnly = true)
  public byte[] png(String shortId, CertificateRenderService renderer) {
    return renderer.renderPng(renderRequest(load(shortId)));
  }

  @Transactional(readOnly = true)
  public byte[] pdf(String shortId, CertificateRenderService renderer) {
    return renderer.renderPdf(renderRequest(load(shortId)));
  }

  private IssuedCertificate load(String shortId) {
    return certificates.findByShortId(shortId.toUpperCase(Locale.ROOT))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
  }

  private CertificateRequest renderRequest(IssuedCertificate certificate) {
    return new CertificateRequest(
        certificate.getRecipientName(),
        certificate.getCertification().getTitle(),
        certificate.getShortId(),
        certificate.getIssuedAt().atZone(ZoneOffset.UTC).toLocalDate(),
        verificationBaseUrl + certificate.getShortId());
  }

  private CertificateDtos.CertificateDetails details(IssuedCertificate certificate) {
    String apiBase = "/api/v1/certificates/issued/" + certificate.getShortId();
    String pngUrl = certificate.getPngObjectKey() == null || r2PublicBaseUrl.isBlank() ? apiBase + "/certificate.png" : r2PublicBaseUrl + "/" + certificate.getPngObjectKey();
    String pdfUrl = certificate.getPdfObjectKey() == null || r2PublicBaseUrl.isBlank() ? apiBase + "/certificate.pdf" : r2PublicBaseUrl + "/" + certificate.getPdfObjectKey();
    return new CertificateDtos.CertificateDetails(
        certificate.getShortId(), certificate.getRecipientName(), certificate.getCertification().getSlug(),
        certificate.getCertification().getTitle(), certificate.getScore(), certificate.getTotalQuestions(),
        certificate.getIssuedAt(), certificate.getStatus(), verificationBaseUrl + certificate.getShortId(),
        pngUrl, pdfUrl);
  }

  private String nextShortId() {
    for (int tries = 0; tries < 10; tries++) {
      StringBuilder value = new StringBuilder("SC-");
      for (int index = 0; index < 10; index++) value.append(SHORT_ID_ALPHABET[random.nextInt(SHORT_ID_ALPHABET.length)]);
      String shortId = value.toString();
      if (!certificates.existsByShortId(shortId)) return shortId;
    }
    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not allocate a certificate ID");
  }
}
