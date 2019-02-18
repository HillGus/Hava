package annotation;

import javax.persistence.Entity;
import javax.persistence.Id;
import hava.annotation.spring.CRUD;

@Entity
@CRUD
public class TestEntity {

  @Id
  private Long id;
}
