package hava.socket.simple.client.listeners;

import hava.socket.simple.client.SimpleClient;

public interface SimpleClientStartListener {


  public void beforeStart(SimpleClient client);

  public void afterStart(SimpleClient client);
}
