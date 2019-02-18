package hava.socket.simple.server.handlers.listeners;

public interface SimpleClientHandlerCompleteListener
    extends SimpleClientHandlerStartListener, SimpleClientHandlerMessagesListener,
    SimpleClientHandlerDisconnectListener, SimpleClientHandlerExceptionListener {

}
