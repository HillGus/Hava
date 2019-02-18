package annotation;

import javax.persistence.Entity;
import javax.persistence.Id;
import hava.annotation.spring.CRUD;

@Entity
@CRUD
public class Test {

  @Id
  private Long id;
}
