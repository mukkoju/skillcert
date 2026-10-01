package online.vaionyx.skillcert.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateController {
  private final CertificateRenderService renderer;
  public CertificateController(CertificateRenderService renderer) { this.renderer = renderer; }

  @GetMapping(value = "/preview.png", produces = MediaType.IMAGE_PNG_VALUE)
  public byte[] previewPng(@RequestParam @NotBlank @Size(max = 30) String recipientName, @RequestParam @NotBlank @Size(max = 30) String courseName) {
    return renderer.renderPng(sample(recipientName, courseName));
  }

  @GetMapping(value = "/preview.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> previewPdf(@RequestParam @NotBlank @Size(max = 30) String recipientName, @RequestParam @NotBlank @Size(max = 30) String courseName) {
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=skillcert-preview.pdf").body(renderer.renderPdf(sample(recipientName, courseName)));
  }

  private CertificateRequest sample(String recipientName, String courseName) {
    String courseKey = courseName.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    if (courseKey.isBlank()) {
      courseKey = "CERT";
    }
    courseKey = courseKey.substring(0, Math.min(4, courseKey.length()));
    String credentialId = "SC-" + courseKey + "-2026-00001";
    return new CertificateRequest(recipientName.trim(), courseName.trim(), credentialId, LocalDate.now(), "https://verify.skillcert.vaionyx.com/" + credentialId);
  }
}
