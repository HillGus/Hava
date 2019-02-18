package hava.socket.simple.server.listeners;

import java.io.IOException;
import hava.socket.simple.server.SimpleServer;

public interface SimpleServerExceptionListener {


  public void clientConnectException(IOException e);

  public void serverCloseException(IOException e, SimpleServer server);
}
