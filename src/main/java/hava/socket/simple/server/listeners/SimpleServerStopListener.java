package hava.socket.simple.server.listeners;

import hava.socket.simple.server.SimpleServer;

public interface SimpleServerStopListener {


  public void beforeStop(SimpleServer server);

  public void afterStop(SimpleServer server);
}
