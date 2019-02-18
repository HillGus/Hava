package socket;

import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;
import hava.socket.simple.server.SimpleServer;
import hava.socket.simple.server.adapters.SimpleServerClientConnectionAdapter;
import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.handlers.adapters.SimpleClientHandlerExceptionAdapter;
import hava.socket.simple.server.handlers.adapters.SimpleClientHandlerMessagesAdapter;
import hava.socket.simple.server.listeners.SimpleServerClientConnectionListener;

public class SimpleServerTest {


  public static void main(String[] args) throws IOException {

    @SuppressWarnings("resource")
    SimpleServer server = new SimpleServer(8912);
    server.useDefaultListener();

    server.addOnClientConnectListener(new SimpleServerClientConnectionListener() {

      @Override
      public void clientConnect(SimpleClient client, SimpleClientHandler clientHandler) {

        clientHandler.addMessagesListener(new SimpleClientHandlerMessagesAdapter() {

          @Override
          public void receiveMessage(Message message, SimpleClient client) {
            
            server.sendMessageToAll(message);
          }      
        });
      }
    });
    
    server.addOnClientConnectListener(new SimpleServerClientConnectionAdapter() {
      
      @Override
      public void clientConnect(SimpleClient client, SimpleClientHandler clientHandler) {
        
        clientHandler.addExceptionListener(new SimpleClientHandlerExceptionAdapter() {
          
          @Override
          public void startException(IOException e, SimpleClientHandler handler) {
            
            server.stop();
          }
        });
      }
    });
    
    server.start();
  }
}