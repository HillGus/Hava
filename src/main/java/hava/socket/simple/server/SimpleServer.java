package hava.socket.simple.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;
import hava.socket.simple.server.handlers.SimpleClientHandler;
import hava.socket.simple.server.listeners.SimpleServerClientConnectionListener;
import hava.socket.simple.server.listeners.SimpleServerCompleteListener;
import hava.socket.simple.server.listeners.SimpleServerDefaultListener;
import hava.socket.simple.server.listeners.SimpleServerExceptionListener;
import hava.socket.simple.server.listeners.SimpleServerStartListener;
import hava.socket.simple.server.listeners.SimpleServerStopListener;

public class SimpleServer extends ServerSocket implements Runnable {


  private boolean started, running;
  private Thread serverThread;

  private Map<String, SimpleClientHandler> clientHandlers = new HashMap<>();

  private List<SimpleServerClientConnectionListener> clientConnectionListeners = new ArrayList<>();
  private List<SimpleServerStartListener> startListeners = new ArrayList<>();
  private List<SimpleServerStopListener> stopListeners = new ArrayList<>();
  private List<SimpleServerExceptionListener> exceptionListeners = new ArrayList<>();


  @Override
  public void run() {

    while (running) {
      try {

        SimpleClient client = this.accept();
        SimpleClientHandler clientHandler = new SimpleClientHandler(client, this);
        clientHandlers.put(client.getInetAddress().getHostAddress(), clientHandler);
        clientHandlers.put(client.getInetAddress().getHostName(), clientHandler);

        onClientConnection(client, clientHandler);

        clientHandler.start();
      } catch (IOException e) {

        onClientConnectionException(e);
      }
    }
  }

  public void sendMessageToAll(Message message) {

    for (Entry<String, SimpleClientHandler> entrada : this.clientHandlers.entrySet()) {

      SimpleClientHandler handler = entrada.getValue();

      handler.sendMessage(message);
    }
  }

  public void sendMessageToAllFrom(Message message, String host) {

    for (Entry<String, SimpleClientHandler> entrada : this.clientHandlers.entrySet()) {

      SimpleClientHandler handler = entrada.getValue();
      String hostAddress = handler.getClient().getInetAddress().getHostAddress();
      String hostName = handler.getClient().getInetAddress().getHostName();

      if ((hostAddress.equals(host)) || (hostName.equals(host)))
        continue;

      handler.sendMessage(message);
    }
  }

  public void sendMessageTo(Message message, String host) {

    SimpleClientHandler handler = this.clientHandlers.get(host);
    handler.sendMessage(message);
  }

  public void sendMessageTo(Message message, List<String> hosts) {

    for (String host : hosts) {

      SimpleClientHandler handler = this.clientHandlers.get(host);
      handler.sendMessage(message);
    }
  }

  public void start() {

    if (!started) {

      onBeforeServerStart();

      started = true;
      running = true;

      serverThread = new Thread(this);
      serverThread.start();

      onAfterServerStart();
    }
  }

  public void stop() {

    if (started) {

      onBeforeServerStop();

      for (Entry<String, SimpleClientHandler> entrada : this.clientHandlers.entrySet())
        entrada.getValue().disconnect();

      if (serverThread != null)
        serverThread.interrupt();

      started = false;
      running = false;
      serverThread = null;

      try {
        close();
      } catch (IOException e) {

        onServerCloseException(e);
      }

      onAfterServerStop();
    }
  }

  public SimpleClient accept() throws IOException {

    if (!isRunning())
      throw new SocketException("Server is not running");

    if (isClosed())
      throw new SocketException("Socket is closed");
    if (!isBound())
      throw new SocketException("Socket is not bound yet");

    SimpleClient socket = new SimpleClient((SocketImpl) null);
    implAccept(socket);
    return socket;
  }

  public void useDefaultListener() {

    addCompleteListener(new SimpleServerDefaultListener());
  }


  private void onClientConnection(SimpleClient client, SimpleClientHandler clientHandler) {

    for (SimpleServerClientConnectionListener listener : this.clientConnectionListeners)
      listener.clientConnect(client, clientHandler);
  }

  private void onBeforeServerStart() {

    for (SimpleServerStartListener listener : this.startListeners)
      listener.beforeStart(this);
  }

  private void onAfterServerStart() {

    for (SimpleServerStartListener listener : this.startListeners)
      listener.afterStart(this);
  }

  private void onBeforeServerStop() {

    for (SimpleServerStopListener listener : this.stopListeners)
      listener.beforeStop(this);
  }

  private void onAfterServerStop() {

    for (SimpleServerStopListener listener : this.stopListeners)
      listener.afterStop(this);
  }

  private void onClientConnectionException(IOException e) {

    for (SimpleServerExceptionListener listener : this.exceptionListeners)
      listener.clientConnectException(e);
  }

  private void onServerCloseException(IOException e) {

    for (SimpleServerExceptionListener listener : this.exceptionListeners)
      listener.serverCloseException(e, this);
  }


  public void addCompleteListener(SimpleServerCompleteListener listener) {

    this.clientConnectionListeners.add(listener);
    this.startListeners.add(listener);
    this.stopListeners.add(listener);
    this.exceptionListeners.add(listener);
  }

  public void addOnClientConnectListener(SimpleServerClientConnectionListener listener) {

    this.clientConnectionListeners.add(listener);
  }

  public void addOnStartListener(SimpleServerStartListener listener) {

    this.startListeners.add(listener);
  }

  public void addOnStopListener(SimpleServerStopListener listener) {

    this.stopListeners.add(listener);
  }

  public void addOnExceptionListener(SimpleServerExceptionListener listener) {

    this.exceptionListeners.add(listener);
  }

  public Map<String, SimpleClientHandler> getClientHandlers() {

    return this.clientHandlers;
  }

  public List<SimpleServerClientConnectionListener> getClientConnectionListeners() {

    return this.clientConnectionListeners;
  }

  public List<SimpleServerStartListener> getStartListeners() {

    return this.startListeners;
  }

  public List<SimpleServerStopListener> getStopListeners() {

    return this.stopListeners;
  }


  public boolean isStarted() {

    return this.started;
  }

  public boolean isRunning() {

    return this.running;
  }


  public SimpleServer(int port) throws IOException {
    super(port);
  }
}
