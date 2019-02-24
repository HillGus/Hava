package hava.annotation.spring.generators.args;

public class NoArgs extends Args<Object> {

  public NoArgs() {
    super(null);
  }

  public static NoArgs of() {
    
    return new NoArgs();
  }
}
