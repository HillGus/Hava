package hava.window.components;

import java.awt.Component;
import java.util.List;
import hava.window.components.listeners.OnAddListener;
import hava.window.components.listeners.OnParentResizeListener;

public interface HComponent {


  public Component getComponent();

  public List<OnAddListener> getAddListeners();

  public List<OnParentResizeListener> getParentResizeListener();


  public default void onAdd() {

    if (getAddListeners() == null)
      return;

    for (OnAddListener listener : getAddListeners()) {

      listener.onAdd(getComponent().getParent());
    }
  }

  public default void onParentResize() {

    if (getParentResizeListener() == null)
      return;

    for (OnParentResizeListener listener : getParentResizeListener()) {

      listener.onParentResize(getComponent().getParent());
    }
  }


  public default void addOnAddListener(OnAddListener listener) {

    if (getAddListeners() == null)
      return;

    this.getAddListeners().add(listener);
  }

  public default void addOnAddListener(Runnable runnable) {

    this.addOnAddListener(parent -> runnable.run());
  }

  public default void addOnParentResizeListener(OnParentResizeListener listener) {

    if (getParentResizeListener() == null)
      return;

    this.getParentResizeListener().add(listener);
  }

  public default void addOnParentResizeListener(Runnable runnable) {

    this.addOnParentResizeListener(parent -> runnable.run());
  }
}
