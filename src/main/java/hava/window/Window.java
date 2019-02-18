package hava.window;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import hava.window.components.HComponent;
import hava.window.components.Panel;

public class Window extends JFrame {
  private static final long serialVersionUID = 1L;


  private Map<String, Container> containers = new HashMap<String, Container>();
  private Panel currentPanel, lastPanel;


  public Window(String title) {

    this(title, 0, 0);
  }

  public Window(String title, int width, int height) {

    super(title);

    setSize(width, height);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(null);
    setLocationRelativeTo(null);

    addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {

        for (Component component : getContentPane().getComponents()) {

          if (component instanceof HComponent)
            ((HComponent) component).onParentResize();
        }
      }
    });
  }


  public Window switchTo(String panelName) {

    if (!containers.containsKey(panelName))
      return this;

    lastPanel = currentPanel;

    remove(currentPanel);
    currentPanel = (Panel) add(containers.get(panelName));

    repaint();

    return this;
  }

  public Window goBack() {

    switchTo(lastPanel.getName());

    return this;
  }

  public Window addContainer(Container container, String name) {

    this.containers.put(name, container);

    return this;
  }

  public Window addPanel(Panel panel) {

    this.containers.put(panel.getName(), panel);

    return this;
  }

  public Window addMainPanel(Panel panel) {

    addPanel(panel).currentPanel = panel;
    add(panel);

    return this;
  }

  @Override
  public Component add(Component comp) {

    super.add(comp);

    if (comp instanceof HComponent) {
      ((HComponent) comp).onAdd();
    }

    return comp;
  }
}
