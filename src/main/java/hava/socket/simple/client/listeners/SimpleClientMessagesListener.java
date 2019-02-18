package hava.socket.simple.client.listeners;

import hava.socket.simple.common.message.Message;

public interface SimpleClientMessagesListener {


  public void receiveMessage(Message message);

  public void beforeSendMessage(Message message);

  public void afterSendMessage(Message message);
}
