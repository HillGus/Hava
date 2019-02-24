package annotation;

import hava.annotation.spring.annotations.CRUD;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@AllArgsConstructor
@CRUD
public class Test {

  @Id
  @GeneratedValue
  private Long id;
  private String nome;
  private String senha;

  public Test() {}
  
  public String getNome() { return this.nome; }
  public String getSenha() { return this.senha; }
}