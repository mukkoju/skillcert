package online.vaionyx.skillcert.instagram;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class InstagramPublishingService {
  private static final Logger log = LoggerFactory.getLogger(InstagramPublishingService.class);
  private static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");
  private static final String API = "https://graph.instagram.com";

  private final InstagramPublicationRepository publications;
  private final RestClient http = RestClient.create();
  private final boolean enabled;
  private final String accessToken;
  private final String accountId;
  private final String mediaUrl;

  public InstagramPublishingService(
      InstagramPublicationRepository publications,
      @Value("${app.instagram.daily-publish-enabled:false}") boolean enabled,
      @Value("${app.instagram.access-token:}") String accessToken,
      @Value("${app.instagram.account-id:}") String accountId,
      @Value("${app.instagram.media-url:}") String mediaUrl) {
    this.publications = publications;
    this.enabled = enabled;
    this.accessToken = accessToken;
    this.accountId = accountId;
    this.mediaUrl = mediaUrl;
  }

  /** Publishes at 9:00 AM India time. Disabled unless explicitly enabled in Render. */
  @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
  @Transactional
  public void publishDaily() {
    if (!enabled) return;
    if (accessToken.isBlank() || accountId.isBlank() || mediaUrl.isBlank()) {
      log.error("Instagram daily publishing is enabled but its configuration is incomplete");
      return;
    }

    LocalDate today = LocalDate.now(INDIA);
    if (publications.existsByPublicationDate(today)) {
      log.info("Instagram post for {} was already published", today);
      return;
    }

    String caption = captions().get(today.getDayOfYear() % captions().size());
    JsonNode container = post("/" + accountId + "/media", form(
        "image_url", mediaUrl,
        "caption", caption,
        "access_token", accessToken));
    String containerId = requiredId(container, "Instagram media container");
    JsonNode published = post("/" + accountId + "/media_publish", form(
        "creation_id", containerId,
        "access_token", accessToken));

    InstagramPublication publication = new InstagramPublication(today);
    publication.markPublished(containerId, requiredId(published, "Instagram post"));
    publications.save(publication);
    log.info("Published SkillCert Instagram post for {}", today);
  }

  private JsonNode post(String path, MultiValueMap<String, String> form) {
    return http.post()
        .uri(API + path)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(JsonNode.class);
  }

  private static MultiValueMap<String, String> form(String... values) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    for (int i = 0; i < values.length; i += 2) form.add(values[i], values[i + 1]);
    return form;
  }

  private static String requiredId(JsonNode response, String description) {
    String id = response == null ? "" : response.path("id").asText();
    if (id.isBlank()) throw new IllegalStateException(description + " was not created by Instagram");
    return id;
  }

  private static List<String> captions() {
    return List.of(
        "Build proof, not just promises.\n\nTake a SkillCert assessment, earn a verifiable certificate, and share your skills with confidence.\n\n#SkillCert #CareerGrowth #SoftwareDeveloper",
        "Your next opportunity may begin with proof of what you know.\n\nAssess your skills. Earn your SkillCert. Share it with confidence.\n\n#SkillCert #Upskilling #TechCareers",
        "Five focused minutes can become a stronger profile.\n\nTake the assessment and unlock your verified SkillCert certificate.\n\n#SkillCert #ProfessionalDevelopment #CareerReady",
        "Skills deserve to be seen.\n\nTurn what you know into a shareable, verifiable SkillCert certificate.\n\n#SkillCert #LinkedInProfile #CareerGrowth",
        "Ready to show what you know?\n\nTake a SkillCert assessment today and add proof of your skills to your next application.\n\n#SkillCert #JobSearch #SoftwareEngineering",
        "Learn. Assess. Verify. Share.\n\nSkillCert helps you turn knowledge into a credential you can be proud to show.\n\n#SkillCert #TechSkills #CareerDevelopment",
        "A certificate is more powerful when it is verifiable.\n\nShow your completed SkillCert assessment on your résumé and LinkedIn.\n\n#SkillCert #ResumeTips #LinkedInTips");
  }
}
