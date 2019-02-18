package hava.socket.simple.client.adapters;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.client.listeners.SimpleClientCompleteListener;
import hava.socket.simple.common.message.Message;

public class SimpleClientCompleteAdapter implements SimpleClientCompleteListener {

  @Override
  public void beforeStart(SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterStart(SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void receiveMessage(Message message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void beforeSendMessage(Message message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterSendMessage(Message message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void beforeDisconnect(SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterDisconnect(SimpleClient client) {
    // TODO Auto-generated method stub

  }

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
