package online.vaionyx.skillcert.certification;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attempt_audits")
public class AttemptAudit {
  @Id @GeneratedValue private UUID id;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "attempt_id", nullable = false, unique = true) private Attempt attempt;
  private String source;
  private String utmSource;
  private String utmMedium;
  private String utmCampaign;
  private String utmContent;
  private String utmTerm;
  @Column(columnDefinition = "text") private String referrer;
  private String browser;
  private String operatingSystem;
  private String deviceType;
  private Boolean inAppBrowser;
  private String country;
  private String state;
  private String city;
  private String timezone;
  private Integer viewportWidth;
  private Instant landingAt;
  private Instant assessmentStartedAt;
  private Instant assessmentCompletedAt;

  protected AttemptAudit() {}
  public AttemptAudit(Attempt attempt, CertificationDtos.SaveAttemptAudit request) {
    this.attempt = attempt;
    this.source = request.source(); this.utmSource = request.utmSource(); this.utmMedium = request.utmMedium(); this.utmCampaign = request.utmCampaign(); this.utmContent = request.utmContent(); this.utmTerm = request.utmTerm();
    this.referrer = request.referrer(); this.browser = request.browser(); this.operatingSystem = request.operatingSystem(); this.deviceType = request.deviceType(); this.inAppBrowser = request.inAppBrowser();
    this.country = request.country(); this.state = request.state(); this.city = request.city(); this.timezone = request.timezone(); this.viewportWidth = request.viewportWidth();
    this.landingAt = request.landingAt(); this.assessmentStartedAt = request.assessmentStartedAt(); this.assessmentCompletedAt = request.assessmentCompletedAt();
  }
}
