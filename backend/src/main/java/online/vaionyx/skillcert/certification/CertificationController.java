package online.vaionyx.skillcert.certification;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import static online.vaionyx.skillcert.certification.CertificationDtos.*;

@RestController @RequestMapping("/api/v1/certifications")
public class CertificationController {
  private final CertificationService service;
  public CertificationController(CertificationService service) { this.service=service; }
  @GetMapping public List<CertificationSummary> list() { return service.list(); }
  @GetMapping("/{slug}/assessment") public Assessment assessment(@PathVariable String slug) { return service.assessment(slug); }
  @PostMapping("/{slug}/attempts") public AttemptResult submit(@PathVariable String slug, @Valid @RequestBody SubmitAttempt request) { return service.submit(slug, request); }
}
