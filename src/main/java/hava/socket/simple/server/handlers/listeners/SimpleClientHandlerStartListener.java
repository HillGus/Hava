package hava.socket.simple.server.handlers.listeners;

import hava.socket.simple.server.handlers.SimpleClientHandler;

public interface SimpleClientHandlerStartListener {


  public void beforeStart(SimpleClientHandler clientHandler);

  public void afterStart(SimpleClientHandler clientHandler);
}
