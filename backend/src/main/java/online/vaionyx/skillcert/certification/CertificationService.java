package online.vaionyx.skillcert.certification;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static online.vaionyx.skillcert.certification.CertificationDtos.*;

@Service
public class CertificationService {
  private final CertificationRepository certifications; private final QuestionRepository questions; private final QuestionOptionRepository options; private final AttemptRepository attempts;
  public CertificationService(CertificationRepository certifications, QuestionRepository questions, QuestionOptionRepository options, AttemptRepository attempts) { this.certifications=certifications; this.questions=questions; this.options=options; this.attempts=attempts; }
  public List<CertificationSummary> list() { return certifications.findByPublishedTrueOrderByTitleAsc().stream().map(this::summary).toList(); }
  public Assessment assessment(String slug) {
    Certification certification = active(slug); List<Question> questionList = questions.findByCertificationIdAndPublishedTrueOrderByPositionAsc(certification.getId());
    return new Assessment(certification.getSlug(), certification.getTitle(), certification.getQuestionCount(), certification.getPassingScore(), questionList.stream().map(q -> new AssessmentQuestion(q.getId(), q.getPrompt(), q.getTheory(), q.getTopic(), q.getDifficulty(), q.getAskedByCompany(), q.getCodeSnippet(), options.findByQuestionIdOrderByPositionAsc(q.getId()).stream().map(o -> new Option(o.getId(), o.getLabel())).toList())).toList());
  }
  public AttemptResult submit(String slug, SubmitAttempt request) {
    Certification certification = active(slug); Map<UUID, UUID> supplied = new HashMap<>(); request.answers().forEach(a -> supplied.put(a.questionId(), a.optionId()));
    List<Question> questionList = questions.findByCertificationIdAndPublishedTrueOrderByPositionAsc(certification.getId()); int score = 0;
    for (Question q : questionList) { UUID optionId = supplied.get(q.getId()); if (optionId != null && options.findByIdAndQuestionId(optionId, q.getId()).map(QuestionOption::isCorrect).orElse(false)) score++; }
    boolean passed = score >= certification.getPassingScore(); Attempt attempt = attempts.save(new Attempt(certification, request.recipientName().trim(), score, questionList.size(), passed)); return new AttemptResult(attempt.getId(), passed, score, questionList.size(), certification.getPassingScore(), passed ? "PAYMENT_REQUIRED" : "NOT_ELIGIBLE");
  }
  @Transactional
  public void saveContact(UUID attemptId, SaveContact request) {
    Attempt attempt = attempts.findById(attemptId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment attempt not found"));
    String mobileDigits = request.mobile().replaceAll("\\D", "");
    if (mobileDigits.length() < 10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid mobile number");
    attempt.setContactDetails(request.email().trim().toLowerCase(Locale.ROOT), request.mobile().trim());
  }
  private Certification active(String slug) { return certifications.findBySlugAndPublishedTrue(slug).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found")); }
  private CertificationSummary summary(Certification c) { return new CertificationSummary(c.getSlug(),c.getTitle(),c.getDescription(),c.getDurationMinutes(),c.getQuestionCount(),c.getPassingScore(),c.getPricePaise(),c.getCategory()); }
}
