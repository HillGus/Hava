package hava.socket.simple.client.listeners;

import hava.socket.simple.client.SimpleClient;

public interface SimpleClientDisconnectListener {


  public void beforeDisconnect(SimpleClient client);

  public void afterDisconnect(SimpleClient client);
}
