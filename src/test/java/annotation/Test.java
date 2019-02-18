package annotation;

import javax.persistence.Entity;
import javax.persistence.Id;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.Filter;

@Entity
@CRUD(filter = @Filter(parameters = {"Teste", "var1"},
                        parametersTypes = {String.class, Integer.class}))
public class Test {

  @Id
  private Long id;
}
