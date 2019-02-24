package hava.annotation.spring.generators.args;

public class Args<A> {
  
  
  private A one;
  
  public A one() {
    
    return this.one;
  }
  
  public Args(A arg1) {
    
    this.one = arg1;
  }
  
  public static NoArgs of() {
    
    return new NoArgs();
  }
  
  public static <B> Args<B> of(B arg1) {
    
    return new Args<B>(arg1);
  }
  
  public static <B, C> TwoArgs<B, C> of (B arg1, C arg2) {
    
    return new TwoArgs<B, C>(arg1, arg2);
  }
  
  public static <B, C, D> ThreeArgs<B, C, D> of (B arg1, C arg2, D arg3) {
    
    return new ThreeArgs<B, C, D>(arg1, arg2, arg3);
  }
}