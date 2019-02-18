package hava.socket.simple.server.handlers.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.server.handlers.SimpleClientHandler;

public interface SimpleClientHandlerExceptionListener {


  public void receiveMessageException(IOException e);

  public void startException(IOException e, SimpleClientHandler handler);

  public void readerCloseException(IOException e, BufferedReader reader);

  public void handlerCloseException(IOException e, SimpleClientHandler handler);
}
