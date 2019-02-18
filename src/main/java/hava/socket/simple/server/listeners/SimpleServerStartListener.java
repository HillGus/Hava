package hava.socket.simple.server.listeners;

import hava.socket.simple.server.SimpleServer;

public interface SimpleServerStartListener {


  public void beforeStart(SimpleServer server);

  public void afterStart(SimpleServer server);
}
