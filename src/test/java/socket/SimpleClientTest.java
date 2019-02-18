package socket;

import java.io.IOException;
import java.net.UnknownHostException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;

public class SimpleClientTest {


  public static void main(String[] args) throws UnknownHostException, IOException {
 
    
    @SuppressWarnings("resource")
    SimpleClient client = new SimpleClient("127.0.0.1", 8912);
    client.useDefaultListener();
    client.start();
    
    client.sendMessage(new Message("Aloooo"));
  }
}