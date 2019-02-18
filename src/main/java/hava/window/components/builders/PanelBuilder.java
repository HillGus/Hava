package hava.window.components.builders;

import java.awt.Container;
import hava.window.components.Configurator;
import hava.window.components.Panel;

@SuppressWarnings("rawtypes")
public class PanelBuilder<PB extends PanelBuilder> {


  private static int quantity = 1;

  private Configurator<Panel> panelConfiguration = p -> {
  };
  protected String name;
  protected int width = 0, height = 0;
  protected int x = 0, y = 0;
  protected int padding = 0, margin = 0;
  private boolean centralize = false;


  public Panel build() {

    quantity++;

    if (name.isEmpty())
      name = "Panel" + Integer.toString(quantity);

    return configurePanel(new Panel(name));
  }

  protected Panel configurePanel(Panel panel) {

    panel.setBounds(x, y, width + padding * 2, height + padding * 2);

    panelConfiguration.configure(panel);

    if (centralize) {

      Runnable centralizer = () -> {
        Container parent = panel.getParent();
        int x = parent.getWidth() / 2 - panel.getWidth() / 2;
        int y = parent.getHeight() / 2 - panel.getHeight() / 2;
        panel.setLocation(x, y);
      };

      panel.addOnAddListener(centralizer);
      panel.addOnParentResizeListener(centralizer);
    }

    return panel;
  }


  public PB name(String name) {

    this.name = name;

    return This();
  }

  public PB width(int width) {

    this.width = width;

    return This();
  }

  public PB height(int height) {

    this.height = height;

    return This();
  }

  public PB size(int width, int height) {

    this.width = width;
    this.height = height;

    return This();
  }

  public PB x(int x) {

    this.x = x;

    return This();
  }

  public PB y(int y) {

    this.y = y;

    return This();
  }

  public PB location(int x, int y) {

    this.x = x;
    this.y = y;

    return This();
  }

  public PB bounds(int x, int y, int width, int height) {

    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    return This();
  }

  public PB centralize() {

    this.centralize = true;

    return This();
  }

  public PB padding(int padding) {

    this.padding = padding;

    return This();
  }

  public PB margin(int margin) {

    this.margin = margin;

    return This();
  }

  public PB panelConfiguration(Configurator<Panel> configurator) {

    this.panelConfiguration = configurator;

    return This();
  }

  @SuppressWarnings("unchecked")
  protected PB This() {

    return (PB) this;
  }
}
