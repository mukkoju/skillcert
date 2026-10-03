package online.vaionyx.skillcert.certificate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/certificates")
public class IssuedCertificateController {
  private final CertificateIssuanceService issuance;
  private final CertificateRenderService renderer;
  private final CertificateStorageService storage;

  public IssuedCertificateController(CertificateIssuanceService issuance, CertificateRenderService renderer, CertificateStorageService storage) {
    this.issuance = issuance;
    this.renderer = renderer;
    this.storage = storage;
  }

  @GetMapping("/verify/{shortId}")
  public CertificateDtos.CertificateDetails verify(@PathVariable String shortId) {
    storage.store(shortId);
    return issuance.find(shortId);
  }

  @GetMapping(value = "/issued/{shortId}/certificate.png", produces = MediaType.IMAGE_PNG_VALUE)
  public byte[] certificatePng(@PathVariable String shortId) {
    return issuance.png(shortId, renderer);
  }

  @GetMapping(value = "/issued/{shortId}/certificate.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> certificatePdf(@PathVariable String shortId) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=skillcert-" + shortId + ".pdf")
        .body(issuance.pdf(shortId, renderer));
  }
}
