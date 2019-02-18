package hava.socket.simple.server.adapters;

import java.io.IOException;
import hava.socket.simple.server.SimpleServer;
import hava.socket.simple.server.listeners.SimpleServerExceptionListener;

public abstract class SimpleServerExceptionAdapter implements SimpleServerExceptionListener {

  @Override
  public void clientConnectException(IOException e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void serverCloseException(IOException e, SimpleServer server) {
    // TODO Auto-generated method stub

  }

}
