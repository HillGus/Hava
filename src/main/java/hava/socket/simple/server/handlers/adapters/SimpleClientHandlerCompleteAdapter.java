package hava.socket.simple.server.handlers.adapters;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;
import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerCompleteListener;

public abstract class SimpleClientHandlerCompleteAdapter
    implements SimpleClientHandlerCompleteListener {

  @Override
  public void beforeStart(SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterStart(SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

  @Override
  public void receiveMessage(Message message, SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void beforeSendMessage(Message message, SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterSendMessage(Message message, SimpleClient client) {
    // TODO Auto-generated method stub

  }

  @Override
  public void beforeDisconnect(SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterDisconnect(SimpleClientHandler clientHandler) {
    // TODO Auto-generated method stub

  }

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
