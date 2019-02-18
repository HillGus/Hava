package hava.window.components.builders;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import hava.window.Window;
import hava.window.components.Button;
import hava.window.components.ButtonPanel;
import hava.window.components.Configurator;
import hava.window.components.Panel;
import hava.window.components.listeners.OnActionListener;

public class ButtonPanelBuilder extends PanelBuilder<ButtonPanelBuilder> {


  private static final int LAYOUT_VERTICAL = 0, LAYOUT_HORIZONTAL = 1;
  private static int quantity = 1;

  private ButtonPanel panel;
  private ArrayList<Button> buttons = new ArrayList<Button>();
  private Configurator<Button> buttonConfiguration = b -> {
  };
  private int bt_width = 0, bt_height = 0;
  protected int layout = LAYOUT_VERTICAL;


  @Override
  public ButtonPanel build() {

    if (name.isEmpty())
      name = "ButtonPanel" + Integer.toString(quantity);

    panel = new ButtonPanel(name);
    configurePanel(panel);

    addButtons();

    quantity++;

    return panel;
  }

  @Override
  protected Panel configurePanel(Panel panel) {

    if (layout == LAYOUT_VERTICAL && height == 0 && bt_height != 0) {

      this.height = (bt_height + margin * 2) * buttons.size();
      this.width = bt_width + margin * 2;

    } else if (layout == LAYOUT_HORIZONTAL && width == 0 && bt_width != 0) {

      this.width = (bt_width + margin * 2) * buttons.size();
      this.height = bt_height + margin * 2;
    }

    super.configurePanel(panel);

    return panel;
  }

  private void addButtons() {

    if (buttons.size() == 0)
      return;

    int buttonsWidth = width - margin * 2, buttonsHeight = height - margin * 2;

    if (layout == LAYOUT_VERTICAL)
      buttonsHeight = (height - margin * 2 * buttons.size()) / buttons.size();
    else
      buttonsWidth = (width - margin * 2 * buttons.size()) / buttons.size();

    for (int i = 0; i < buttons.size(); i++) {

      Button button = buttons.get(i);

      int buttonX = padding + margin, buttonY = padding + margin;

      if (layout == LAYOUT_VERTICAL)
        buttonY = (buttonsHeight + margin * 2) * i + padding + margin;
      else
        buttonX = (buttonsWidth + margin * 2) * i + padding + margin;

      button.setLocation(buttonX, buttonY);
      button.setSize(buttonsWidth, buttonsHeight);

      buttonConfiguration.configure(button);

      panel.add(button);
    }
  }


  public ButtonPanelBuilder gotoButton(String buttonText, String containerName) {

    Button button = new Button(buttonText);
    button.addActionListener(ae -> {
      JFrame window = (JFrame) SwingUtilities.getWindowAncestor(panel);
      if (window instanceof Window)
        ((Window) window).switchTo(containerName);
    });

    buttons.add(button);

    return this;
  }

  public ButtonPanelBuilder gotoButton(String buttonText) {

    return this.gotoButton(buttonText, buttonText);
  }


  public ButtonPanelBuilder switchButton(String buttonText, String switchedText,
      OnActionListener action1, OnActionListener action2) {

    Button button = new Button(buttonText);
    button.switchBetween(buttonText, switchedText, action1, action2);

    buttons.add(button);

    return this;
  }

  public ButtonPanelBuilder switchButton(String buttonText, String switchedText,
      ActionListener action1, ActionListener action2) {

    return this.switchButton(buttonText, switchedText, (ae, button) -> action1.actionPerformed(ae),
        (ae, button) -> action2.actionPerformed(ae));
  }

  public ButtonPanelBuilder switchButton(String buttonText, String switchedText, Runnable action1,
      Runnable action2) {

    return this.switchButton(buttonText, switchedText, (ae, button) -> action1.run(),
        (ae, button) -> action2.run());
  }


  public ButtonPanelBuilder actionButton(String buttonText, OnActionListener action) {

    Button button = new Button(buttonText);
    button.action(action);

    buttons.add(button);

    return this;
  }

  public ButtonPanelBuilder actionButton(String buttonText, ActionListener action) {

    return this.actionButton(buttonText, (ae, button) -> action.actionPerformed(ae));
  }

  public ButtonPanelBuilder actionButton(String buttonText, Runnable runnable) {

    return this.actionButton(buttonText, (ae, button) -> runnable.run());
  }


  public ButtonPanelBuilder backButton(String buttonText) {

    Button button = new Button(buttonText);
    button.addActionListener(ae -> {
      JFrame window = (JFrame) SwingUtilities.getWindowAncestor(panel);
      if (window instanceof Window)
        ((Window) window).goBack();
    });

    buttons.add(button);

    return this;
  }


  public ButtonPanelBuilder name(String name) {

    this.name = name;

    return this;
  }

  public ButtonPanelBuilder horizontal() {

    this.layout = LAYOUT_HORIZONTAL;

    return this;
  }

  public ButtonPanelBuilder buttons_size(int width, int height) {

    this.bt_width = width;
    this.bt_height = height;

    return this;
  }

  public ButtonPanelBuilder buttonConfiguration(Configurator<Button> configurator) {

    this.buttonConfiguration = configurator;

    return this;
  }
}
