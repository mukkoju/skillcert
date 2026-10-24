package online.vaionyx.skillcert.recovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import online.vaionyx.skillcert.certification.Attempt;
import online.vaionyx.skillcert.certification.AttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentRecoveryService {
  private static final URI RESEND_EMAILS = URI.create("https://api.resend.com/emails");
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final List<ReminderStage> STAGES = List.of(
      new ReminderStage("FIRST", Duration.ofMinutes(3), "Your SkillCert result is ready — unlock your certificate"),
      new ReminderStage("SECOND", Duration.ofHours(6), "Your verified SkillCert certificate is ready to unlock"),
      new ReminderStage("FINAL", Duration.ofHours(24), "Final reminder: unlock your SkillCert certificate"));

  private final AttemptRepository attempts;
  private final PaymentRecoveryEmailRepository reminders;
  private final PaymentRecoverySettingsRepository settings;
  private final ObjectMapper json;
  private final String apiKey;
  private final String fromEmail;
  private final String publicWebUrl;
  private final boolean enabled;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  public PaymentRecoveryService(
      AttemptRepository attempts,
      PaymentRecoveryEmailRepository reminders,
      PaymentRecoverySettingsRepository settings,
      ObjectMapper json,
      @Value("${app.resend-api-key:}") String apiKey,
      @Value("${app.certificate-from-email:}") String fromEmail,
      @Value("${app.public-web-url}") String publicWebUrl,
      @Value("${app.payment-recovery-enabled:false}") boolean enabled) {
    this.attempts = attempts;
    this.reminders = reminders;
    this.settings = settings;
    this.json = json;
    this.apiKey = apiKey;
    this.fromEmail = fromEmail;
    this.publicWebUrl = publicWebUrl.replaceAll("/+$", "");
    this.enabled = enabled;
  }

  @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
  public void sendDueReminders() {
    if (!enabled || apiKey.isBlank() || fromEmail.isBlank()) return;
    Instant recoveryStartedAt = settings.findById((short) 1)
        .orElseGet(() -> settings.saveAndFlush(new PaymentRecoverySettings(Instant.now())))
        .getActivatedAt();
    for (ReminderStage stage : STAGES) {
      List<Attempt> attemptsDue = attempts.findUnpaidCompletedBefore(recoveryStartedAt, Instant.now().minus(stage.delay()));
      for (Attempt attempt : attemptsDue) {
        try {
          sendIfNeeded(attempt, stage);
        } catch (Exception ignored) {
          // A later scheduler pass retries transient Resend or network failures.
        }
      }
    }
  }

  @Transactional
  public void sendIfNeeded(Attempt attempt, ReminderStage stage) {
    if (reminders.existsByAttemptIdAndStage(attempt.getId(), stage.code())) return;
    PaymentRecoveryEmail reminder = new PaymentRecoveryEmail(attempt, stage.code(), newToken());
    try {
      reminders.saveAndFlush(reminder);
    } catch (DataIntegrityViolationException alreadyCreated) {
      return;
    }
    send(reminder, stage);
    reminder.markSent();
  }

  public PaymentRecoveryEmail requireByToken(String token) {
    return reminders.findByResumeToken(token)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This result link is no longer available"));
  }

  private void send(PaymentRecoveryEmail reminder, ReminderStage stage) {
    Attempt attempt = reminder.getAttempt();
    String url = publicWebUrl + "/" + attempt.getCertification().getSlug() + "?resume=" + reminder.getResumeToken();
    try {
      ObjectNode payload = json.createObjectNode();
      payload.put("from", fromEmail);
      ArrayNode recipients = payload.putArray("to");
      recipients.add(attempt.getRecipientEmail());
      payload.put("subject", stage.subject());
      payload.put("html", html(attempt, stage, url));
      HttpRequest request = HttpRequest.newBuilder(RESEND_EMAILS)
          .timeout(Duration.ofSeconds(30))
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .header("Idempotency-Key", "skillcert-payment-recovery/" + attempt.getId() + "/" + stage.code())
          .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
          .build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Resend returned " + response.statusCode());
      }
    } catch (Exception failure) {
      throw new IllegalStateException("Payment recovery email could not be delivered", failure);
    }
  }

  private String html(Attempt attempt, ReminderStage stage, String resumeUrl) {
    String name = escape(attempt.getRecipientName());
    String course = escape(attempt.getCertification().getTitle());
    String url = escape(resumeUrl);
    String intro = switch (stage.code()) {
      case "SECOND" -> "Your certificate is still ready to unlock.";
      case "FINAL" -> "This is a final reminder to complete your certificate.";
      default -> "Your result and verified certificate are ready to unlock.";
    };
    return "<div style=\"font-family:Arial,sans-serif;color:#10213b;line-height:1.55;max-width:560px;margin:auto\">"
        + "<h1 style=\"margin-bottom:8px\">Your SkillCert result is ready</h1>"
        + "<p>Hi " + name + ",</p>"
        + "<p>You completed the <strong>" + course + "</strong> assessment — great work.</p>"
        + "<p>" + intro + "</p>"
        + "<p style=\"margin:28px 0\"><a href=\"" + url + "\" style=\"display:inline-block;background:#0b9fe8;color:#fff;padding:14px 22px;border-radius:8px;text-decoration:none;font-weight:bold\">Unlock &amp; Download My Certificate</a></p>"
        + "<p style=\"color:#617089;font-size:13px\">If you have already completed payment, you can ignore this email.</p>"
        + "<p>— SkillCert by VAIONYX</p></div>";
  }

  private String newToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String escape(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  private record ReminderStage(String code, Duration delay, String subject) {}
}
