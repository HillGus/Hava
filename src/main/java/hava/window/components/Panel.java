package hava.window.components;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import hava.window.components.listeners.OnAddListener;
import hava.window.components.listeners.OnParentResizeListener;

public class Panel extends JPanel implements HComponent {
  private static final long serialVersionUID = 1L;


  protected List<OnAddListener> onAddListeners = new ArrayList<>();
  protected List<OnParentResizeListener> onParentResizeListeners = new ArrayList<>();

  protected String name;


  public Panel(String name) {

    super();

    this.name = name;
    setLayout(null);
  }

  public String getName() {

    return this.name;
  }

  @Override
  public Component getComponent() {
    return this;
  }

  @Override
  public List<OnAddListener> getAddListeners() {
    return this.onAddListeners;
  }

  @Override
  public List<OnParentResizeListener> getParentResizeListener() {
    return this.onParentResizeListeners;
  }
}
