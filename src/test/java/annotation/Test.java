package annotation;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Test {

  @Id
  private Long id;
  private String nome;
  private String senha;
}
