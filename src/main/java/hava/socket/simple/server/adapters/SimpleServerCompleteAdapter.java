package hava.socket.simple.server.adapters;

import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.server.SimpleServer;
import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.listeners.SimpleServerCompleteListener;

public abstract class SimpleServerCompleteAdapter implements SimpleServerCompleteListener {

  @Override
  public void beforeStart(SimpleServer server) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterStart(SimpleServer server) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clientConnect(SimpleClient client, SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

  @Override
  public void beforeStop(SimpleServer server) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterStop(SimpleServer server) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clientConnectException(IOException e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void serverCloseException(IOException e, SimpleServer server) {
    // TODO Auto-generated method stub

  }

}
