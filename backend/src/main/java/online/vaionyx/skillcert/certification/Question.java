package online.vaionyx.skillcert.certification;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "questions")
public class Question {
  @Id @GeneratedValue private UUID id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="certification_id", nullable=false) private Certification certification;
  @Column(nullable=false, columnDefinition="text") private String prompt;
  @Column(columnDefinition="text") private String theory;
  @Column(nullable=false) private int position;
  @Column(nullable=false) private boolean published;
  public UUID getId(){return id;} public String getPrompt(){return prompt;} public String getTheory(){return theory;} public int getPosition(){return position;}
}
