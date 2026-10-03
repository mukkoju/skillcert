package online.vaionyx.skillcert.certification;
import jakarta.validation.constraints.*;
import java.util.*;

public final class CertificationDtos {
  private CertificationDtos() {}
  public record CertificationSummary(String slug, String title, String description, int durationMinutes, int questionCount, int passingScore, int pricePaise) {}
  public record Option(UUID id, String label) {}
  public record AssessmentQuestion(UUID id, String prompt, String theory, List<Option> options) {}
  public record Assessment(String slug, String title, int questionCount, int passingScore, List<AssessmentQuestion> questions) {}
  public record Answer(@NotNull UUID questionId, @NotNull UUID optionId) {}
  public record SubmitAttempt(@NotBlank String recipientName, @NotEmpty List<Answer> answers) {}
  public record SaveContact(@NotBlank @Email String email, @NotBlank String mobile) {}
  public record AttemptResult(UUID attemptId, boolean passed, int score, int total, int passingScore, String credentialStatus) {}
}
