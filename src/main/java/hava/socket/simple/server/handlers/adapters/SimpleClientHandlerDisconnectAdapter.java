package hava.socket.simple.server.handlers.adapters;

import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerDisconnectListener;

public abstract class SimpleClientHandlerDisconnectAdapter
    implements SimpleClientHandlerDisconnectListener {

  @Override
  public void beforeDisconnect(SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterDisconnect(SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

}
