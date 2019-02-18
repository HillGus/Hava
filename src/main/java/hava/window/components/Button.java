package hava.window.components;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import hava.window.components.listeners.OnActionListener;
import hava.window.components.listeners.OnAddListener;
import hava.window.components.listeners.OnParentResizeListener;

public class Button extends JButton implements HComponent {
  private static final long serialVersionUID = 1L;


  private boolean firstAction = true;


  public Button(String text) {

    super(text);
  }

  public Button action(OnActionListener action) {

    for (ActionListener ae : this.getActionListeners())
      this.removeActionListener(ae);

    this.addAction(action);

    return this;
  }

  public Button action(ActionListener action) {

    return this.action((ae, button) -> action.actionPerformed(ae));
  }

  public Button addAction(OnActionListener action) {

    this.addActionListener(ae -> action.onAction(ae, this));

    return this;
  }

  public Button switchBetween(String text1, String text2, OnActionListener action1,
      OnActionListener action2) {

    this.addActionListener(ae -> {
      if (this.firstAction) {
        action1.onAction(ae, this);
        this.setText(text2);
        this.firstAction = false;
      } else {
        action2.onAction(ae, this);
        this.setText(text1);
        this.firstAction = true;
      }
    });

    return this;
  }

  @Override
  public Component getComponent() {
    return this;
  }

  @Override
  public List<OnAddListener> getAddListeners() {
    return null;
  }

  @Override
  public List<OnParentResizeListener> getParentResizeListener() {
    return null;
  }
}
