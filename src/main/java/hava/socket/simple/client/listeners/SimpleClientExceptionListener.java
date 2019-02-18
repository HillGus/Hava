package hava.socket.simple.client.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.client.SimpleClient;

public interface SimpleClientExceptionListener {


  public void receiveMessageException(IOException e);

  public void startException(IOException e, SimpleClient client);

  public void readerCloseException(IOException e, BufferedReader reader);

  public void clientCloseException(IOException e, SimpleClient client);
}
