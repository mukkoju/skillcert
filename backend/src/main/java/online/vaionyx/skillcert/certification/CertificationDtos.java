package online.vaionyx.skillcert.certification;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class CertificationDtos {
  private CertificationDtos() {}
  public record CertificationSummary(String slug, String title, String description, int durationMinutes, int questionCount, int passingScore, int pricePaise, String category) {}
  public record Option(UUID id, String label) {}
  public record AssessmentQuestion(UUID id, String prompt, String theory, String topic, String difficulty, String askedByCompany, String codeSnippet, List<Option> options) {}
  public record Assessment(String slug, String title, int questionCount, int passingScore, List<AssessmentQuestion> questions) {}
  public record Answer(@NotNull UUID questionId, @NotNull UUID optionId) {}
  public record SubmitAttempt(@NotBlank String recipientName, @NotEmpty List<Answer> answers) {}
  public record SaveContact(@NotBlank @Email String email, @NotBlank String mobile) {}
  public record SaveAttemptAudit(@Size(max=64) String source, @Size(max=160) String utmSource, @Size(max=160) String utmMedium, @Size(max=255) String utmCampaign, @Size(max=255) String utmContent, @Size(max=255) String utmTerm, @Size(max=4000) String referrer, @Size(max=80) String browser, @Size(max=80) String operatingSystem, @Size(max=32) String deviceType, Boolean inAppBrowser, @Size(max=80) String country, @Size(max=120) String state, @Size(max=120) String city, @Size(max=80) String timezone, @Positive Integer viewportWidth, Instant landingAt, Instant assessmentStartedAt, Instant assessmentCompletedAt) {}
  public record AttemptResult(UUID attemptId, boolean passed, int score, int total, int passingScore, String credentialStatus, int pricePaise, String priceVariant) {}
  public record RecoveryAttempt(String certificationSlug, String recipientName, String recipientEmail, String recipientMobile, AttemptResult result) {}
}
