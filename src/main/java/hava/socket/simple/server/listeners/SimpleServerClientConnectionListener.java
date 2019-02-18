package hava.socket.simple.server.listeners;

import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.server.handlers.SimpleClientHandler;

public interface SimpleServerClientConnectionListener {


  public void clientConnect(SimpleClient client, SimpleClientHandler clientHandler);
}
