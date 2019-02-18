package hava.window.components;

public interface Configurator<Component extends HComponent> {

  public void configure(Component c);
}
