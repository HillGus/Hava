package hava.socket.simple.server.handlers.adapters;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerExceptionListener;

public abstract class SimpleClientHandlerExceptionAdapter
    implements SimpleClientHandlerExceptionListener {

  @Override
  public void receiveMessageException(IOException e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void startException(IOException e, SimpleClientHandler handler) {
    // TODO Auto-generated method stub

  }

  @Override
  public void readerCloseException(IOException e, BufferedReader reader) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handlerCloseException(IOException e, SimpleClientHandler handler) {
    // TODO Auto-generated method stub

  }

}
