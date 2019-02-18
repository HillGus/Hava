package hava.socket.simple.server.listeners;

public interface SimpleServerCompleteListener extends SimpleServerStartListener,
    SimpleServerClientConnectionListener, SimpleServerStopListener, SimpleServerExceptionListener {

}
