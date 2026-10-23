package online.vaionyx.skillcert.certificate;

import java.net.URI;
import java.time.ZoneOffset;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CertificateStorageService {
  private final IssuedCertificateRepository certificates;
  private final CertificateRenderService renderer;
  private final String accountId;
  private final String accessKeyId;
  private final String secretAccessKey;
  private final String bucket;
  private final String publicBaseUrl;
  private final String verificationBaseUrl;

  public CertificateStorageService(
      IssuedCertificateRepository certificates,
      CertificateRenderService renderer,
      @Value("${app.r2-account-id:}") String accountId,
      @Value("${app.r2-access-key-id:}") String accessKeyId,
      @Value("${app.r2-secret-access-key:}") String secretAccessKey,
      @Value("${app.r2-bucket-name:}") String bucket,
      @Value("${app.r2-public-base-url:}") String publicBaseUrl,
      @Value("${app.certificate-verification-base-url:https://skillcert.vaionyxsolutions.online/verify/}") String verificationBaseUrl) {
    this.certificates = certificates;
    this.renderer = renderer;
    this.accountId = accountId;
    this.accessKeyId = accessKeyId;
    this.secretAccessKey = secretAccessKey;
    this.bucket = bucket;
    this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    this.verificationBaseUrl = verificationBaseUrl.endsWith("/") ? verificationBaseUrl : verificationBaseUrl + "/";
  }

  @Transactional
  public void store(String shortId) {
    IssuedCertificate certificate = certificates.findWithLockByShortId(shortId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
    if (certificate.getPngObjectKey() != null && certificate.getPdfObjectKey() != null) return;
    requireConfigured();
    CertificateRequest request = request(certificate);
    byte[] png = renderer.renderPng(request);
    byte[] pdf = renderer.renderPdf(request);
    String baseKey = "certificates/" + certificate.getShortId();
    String pngKey = baseKey + ".png";
    String pdfKey = baseKey + ".pdf";
    try (S3Client r2 = client()) {
      r2.putObject(PutObjectRequest.builder().bucket(bucket).key(pngKey).contentType("image/png").cacheControl("public, max-age=31536000, immutable").build(), RequestBody.fromBytes(png));
      r2.putObject(PutObjectRequest.builder().bucket(bucket).key(pdfKey).contentType("application/pdf").contentDisposition("inline; filename=SkillCert-" + certificate.getShortId() + ".pdf").cacheControl("public, max-age=31536000, immutable").build(), RequestBody.fromBytes(pdf));
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Certificate storage could not be reached");
    }
    certificate.markStored(pngKey, pdfKey);
  }

  @Transactional(readOnly = true)
  public byte[] loadPdf(String shortId) {
    IssuedCertificate certificate = certificates.findByShortId(shortId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
    if (certificate.getPdfObjectKey() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Certificate file is still being prepared");
    requireConfigured();
    try (S3Client r2 = client()) {
      ResponseBytes<GetObjectResponse> response = r2.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(certificate.getPdfObjectKey()).build());
      return response.asByteArray();
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stored certificate could not be loaded");
    }
  }

  public String publicUrl(String objectKey) {
    return objectKey == null || publicBaseUrl.isBlank() ? null : publicBaseUrl + "/" + objectKey;
  }

  private CertificateRequest request(IssuedCertificate certificate) {
    return new CertificateRequest(certificate.getRecipientName(), certificate.getCertification().getTitle(), certificate.getShortId(),
        certificate.getIssuedAt().atZone(ZoneOffset.UTC).toLocalDate(), verificationBaseUrl + certificate.getShortId());
  }

  private S3Client client() {
    return S3Client.builder().region(Region.of("auto")).endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))).build();
  }

  private void requireConfigured() {
    if (accountId.isBlank() || accessKeyId.isBlank() || secretAccessKey.isBlank() || bucket.isBlank() || publicBaseUrl.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Certificate storage is not configured");
    }
  }
}
