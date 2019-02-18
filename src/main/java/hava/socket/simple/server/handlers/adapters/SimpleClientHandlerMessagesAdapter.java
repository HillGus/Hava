package hava.socket.simple.server.handlers.adapters;

import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerMessagesListener;

public abstract class SimpleClientHandlerMessagesAdapter
    implements SimpleClientHandlerMessagesListener {

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

}
