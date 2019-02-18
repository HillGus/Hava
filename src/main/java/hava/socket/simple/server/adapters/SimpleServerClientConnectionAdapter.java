package hava.socket.simple.server.adapters;

import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.listeners.SimpleServerClientConnectionListener;

public abstract class SimpleServerClientConnectionAdapter
    implements SimpleServerClientConnectionListener {

  @Override
  public void clientConnect(SimpleClient client, SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

}
