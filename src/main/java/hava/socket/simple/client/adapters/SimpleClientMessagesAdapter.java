package hava.socket.simple.client.adapters;

import hava.socket.simple.client.listeners.SimpleClientMessagesListener;
import hava.socket.simple.common.message.Message;

public abstract class SimpleClientMessagesAdapter implements SimpleClientMessagesListener {

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

}
