package hava.socket.simple.server.handlers.listeners;

import hava.socket.simple.server.handlers.SimpleClientHandler;

public interface SimpleClientHandlerDisconnectListener {


  public void beforeDisconnect(SimpleClientHandler clientHandler);

  public void afterDisconnect(SimpleClientHandler clientHandler);
}
