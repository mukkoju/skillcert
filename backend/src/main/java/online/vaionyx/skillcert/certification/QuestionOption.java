package online.vaionyx.skillcert.certification;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "question_options")
public class QuestionOption {
  @Id @GeneratedValue private UUID id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="question_id", nullable=false) private Question question;
  @Column(nullable=false, columnDefinition="text") private String label;
  @Column(nullable=false) private int position;
  @Column(nullable=false) private boolean correct;
  public UUID getId(){return id;} public String getLabel(){return label;} public int getPosition(){return position;} public boolean isCorrect(){return correct;}
}
