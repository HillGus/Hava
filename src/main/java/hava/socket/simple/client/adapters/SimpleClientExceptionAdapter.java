package hava.socket.simple.client.adapters;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.client.listeners.SimpleClientExceptionListener;

public abstract class SimpleClientExceptionAdapter implements SimpleClientExceptionListener {

  @Override
  public void receiveMessageException(IOException e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void startException(IOException e, SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void readerCloseException(IOException e, BufferedReader reader) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clientCloseException(IOException e, SimpleClient client) {
    // TODO Auto-generated method stub

  }

}
