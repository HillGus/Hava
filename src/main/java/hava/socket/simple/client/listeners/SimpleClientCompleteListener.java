package hava.socket.simple.client.listeners;

public interface SimpleClientCompleteListener extends SimpleClientStartListener,
    SimpleClientMessagesListener, SimpleClientDisconnectListener, SimpleClientExceptionListener {

}
