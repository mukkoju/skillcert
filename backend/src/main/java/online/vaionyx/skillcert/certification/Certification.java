package online.vaionyx.skillcert.certification;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "certifications")
public class Certification {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false, unique=true) private String slug;
  @Column(nullable=false) private String title;
  @Column(nullable=false, columnDefinition="text") private String description;
  @Column(nullable=false) private int durationMinutes;
  @Column(nullable=false) private int questionCount;
  @Column(nullable=false) private int passingScore;
  @Column(nullable=false) private int pricePaise;
  @Column(nullable=false) private boolean published;
  public UUID getId(){return id;} public String getSlug(){return slug;} public String getTitle(){return title;} public String getDescription(){return description;} public int getDurationMinutes(){return durationMinutes;} public int getQuestionCount(){return questionCount;} public int getPassingScore(){return passingScore;} public int getPricePaise(){return pricePaise;} public boolean isPublished(){return published;}
}
