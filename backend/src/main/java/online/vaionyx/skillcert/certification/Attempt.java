package online.vaionyx.skillcert.certification;
import jakarta.persistence.*;
import java.time.Instant; import java.util.UUID;
@Entity @Table(name="attempts") public class Attempt {
  @Id @GeneratedValue private UUID id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="certification_id", nullable=false) private Certification certification;
  @Column(nullable=false) private String recipientName; @Column(name="recipient_email") private String recipientEmail; @Column(name="recipient_mobile") private String recipientMobile; @Column(nullable=false) private int score; @Column(nullable=false) private int totalQuestions; @Column(nullable=false) private boolean passed; @Column(nullable=false) private Instant createdAt;
  protected Attempt(){} public Attempt(Certification c,String name,int score,int total,boolean passed){this.certification=c;this.recipientName=name;this.score=score;this.totalQuestions=total;this.passed=passed;this.createdAt=Instant.now();}
  public UUID getId(){return id;} public Certification getCertification(){return certification;} public String getRecipientName(){return recipientName;} public String getRecipientEmail(){return recipientEmail;} public int getScore(){return score;} public int getTotalQuestions(){return totalQuestions;} public boolean isPassed(){return passed;}
  public void setContactDetails(String email, String mobile){this.recipientEmail=email;this.recipientMobile=mobile;}
}
