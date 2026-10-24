package online.vaionyx.skillcert.certification;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import online.vaionyx.skillcert.payment.PaymentRepository;
import online.vaionyx.skillcert.recovery.PaymentRecoveryEmail;
import online.vaionyx.skillcert.recovery.PaymentRecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static online.vaionyx.skillcert.certification.CertificationDtos.*;

@Service
public class CertificationService {
  private final CertificationRepository certifications; private final QuestionRepository questions; private final QuestionOptionRepository options; private final AttemptRepository attempts; private final AttemptAuditRepository attemptAudits; private final PaymentRecoveryService recovery; private final PaymentRepository payments;
  public CertificationService(CertificationRepository certifications, QuestionRepository questions, QuestionOptionRepository options, AttemptRepository attempts, AttemptAuditRepository attemptAudits, PaymentRecoveryService recovery, PaymentRepository payments) { this.certifications=certifications; this.questions=questions; this.options=options; this.attempts=attempts; this.attemptAudits=attemptAudits; this.recovery=recovery; this.payments=payments; }
  public List<CertificationSummary> list() { return certifications.findByPublishedTrueOrderByTitleAsc().stream().map(this::summary).toList(); }
  public Assessment assessment(String slug) {
    Certification certification = active(slug); List<Question> questionList = assessmentQuestions(certification);
    return new Assessment(certification.getSlug(), certification.getTitle(), certification.getQuestionCount(), certification.getPassingScore(), questionList.stream().map(q -> new AssessmentQuestion(q.getId(), q.getPrompt(), q.getTheory(), q.getTopic(), q.getDifficulty(), q.getAskedByCompany(), q.getCodeSnippet(), options.findByQuestionIdOrderByPositionAsc(q.getId()).stream().map(o -> new Option(o.getId(), o.getLabel())).toList())).toList());
  }
  public AttemptResult submit(String slug, SubmitAttempt request) {
    Certification certification = active(slug); Map<UUID, UUID> supplied = new HashMap<>(); request.answers().forEach(a -> supplied.put(a.questionId(), a.optionId()));
    List<Question> questionList = questions.findByIdInAndCertificationIdAndPublishedTrue(supplied.keySet(), certification.getId());
    if (questionList.size() != certification.getQuestionCount() || supplied.size() != certification.getQuestionCount()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete every assessment question before submitting");
    }
    if ("spring-boot-foundations".equals(certification.getSlug()) && !hasSpringBootMix(questionList)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submit the complete Spring Boot assessment question set");
    }
    int score = 0;
    for (Question q : questionList) { UUID optionId = supplied.get(q.getId()); if (optionId != null && options.findByIdAndQuestionId(optionId, q.getId()).map(QuestionOption::isCorrect).orElse(false)) score++; }
    boolean passed = score >= certification.getPassingScore();
    boolean variantA = certification.isPriceExperimentEnabled() && ThreadLocalRandom.current().nextBoolean();
    int offeredPricePaise = certification.isPriceExperimentEnabled() ? (variantA ? certification.getExperimentPriceAPaise() : certification.getExperimentPriceBPaise()) : certification.getPricePaise();
    String priceVariant = certification.isPriceExperimentEnabled() ? (variantA ? "A" : "B") : "FIXED";
    Attempt attempt = attempts.save(new Attempt(certification, request.recipientName().trim(), score, questionList.size(), passed, offeredPricePaise, priceVariant));
    return new AttemptResult(attempt.getId(), passed, score, questionList.size(), certification.getPassingScore(), passed ? "PAYMENT_REQUIRED" : "NOT_ELIGIBLE", offeredPricePaise, priceVariant);
  }
  @Transactional
  public void saveContact(UUID attemptId, SaveContact request) {
    Attempt attempt = attempts.findById(attemptId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment attempt not found"));
    String mobileDigits = request.mobile().replaceAll("\\D", "");
    if (mobileDigits.length() < 10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid mobile number");
    attempt.setContactDetails(request.email().trim().toLowerCase(Locale.ROOT), request.mobile().trim());
  }
  @Transactional(readOnly = true)
  public RecoveryAttempt recover(String token) {
    PaymentRecoveryEmail reminder = recovery.requireByToken(token);
    Attempt attempt = reminder.getAttempt();
    if (payments.existsByAttemptIdAndStatus(attempt.getId(), "CAPTURED")) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Your certificate has already been issued");
    }
    int passingScore = attempt.getCertification().getPassingScore();
    return new RecoveryAttempt(
        attempt.getCertification().getSlug(),
        attempt.getRecipientName(),
        attempt.getRecipientEmail(),
        attempt.getRecipientMobile(),
        new AttemptResult(attempt.getId(), attempt.isPassed(), attempt.getScore(), attempt.getTotalQuestions(), passingScore, "PAYMENT_REQUIRED", attempt.getOfferedPricePaise(), attempt.getPriceVariant()));
  }
  @Transactional
  public void saveAudit(UUID attemptId, SaveAttemptAudit request) {
    if (attemptAudits.existsByAttemptId(attemptId)) return;
    Attempt attempt = attempts.findById(attemptId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment attempt not found"));
    attemptAudits.save(new AttemptAudit(attempt, request));
  }
  private Certification active(String slug) { return certifications.findBySlugAndPublishedTrue(slug).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification not found")); }
  private List<Question> assessmentQuestions(Certification certification) {
    if (!"spring-boot-foundations".equals(certification.getSlug())) return questions.findByCertificationIdAndPublishedTrueOrderByPositionAsc(certification.getId());
    List<Question> selected = new ArrayList<>();
    selected.addAll(questions.pickRandomByDifficulty(certification.getId(), "Level 1", 3));
    selected.addAll(questions.pickRandomByDifficulty(certification.getId(), "Level 2", 4));
    selected.addAll(questions.pickRandomByDifficulty(certification.getId(), "Level 3", 3));
    if (selected.size() != 10) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Spring Boot assessment question bank is unavailable");
    Collections.shuffle(selected);
    return selected;
  }
  private boolean hasSpringBootMix(List<Question> questionList) {
    return questionList.stream().filter(q -> "Level 1".equals(q.getDifficulty())).count() == 3
        && questionList.stream().filter(q -> "Level 2".equals(q.getDifficulty())).count() == 4
        && questionList.stream().filter(q -> "Level 3".equals(q.getDifficulty())).count() == 3;
  }
  private CertificationSummary summary(Certification c) { return new CertificationSummary(c.getSlug(),c.getTitle(),c.getDescription(),c.getDurationMinutes(),c.getQuestionCount(),c.getPassingScore(),c.getPricePaise(),c.getCategory()); }
}
