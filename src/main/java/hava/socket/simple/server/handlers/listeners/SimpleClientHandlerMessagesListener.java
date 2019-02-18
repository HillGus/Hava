package hava.socket.simple.server.handlers.listeners;

import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;

public interface SimpleClientHandlerMessagesListener {


  public void receiveMessage(Message message, SimpleClient client);

  public void beforeSendMessage(Message message, SimpleClient client);

  public void afterSendMessage(Message message, SimpleClient client);
}
