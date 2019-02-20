package annotation;

import javax.persistence.Entity;
import javax.persistence.Id;
import hava.annotation.spring.annotations.CRUD;
import hava.annotation.spring.annotations.Filter;

@Entity
@CRUD(filter = @Filter(fields = "*", likeType = Filter.LikeType.NONE))
public class Test {

  @Id
  private Long id;
  private String nome;
}
