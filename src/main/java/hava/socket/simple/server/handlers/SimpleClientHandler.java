package hava.socket.simple.server.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;
import hava.socket.simple.server.SimpleServer;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerCompleteListener;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerDefaultListener;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerDisconnectListener;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerExceptionListener;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerMessagesListener;
import hava.socket.simple.server.handlers.listeners.SimpleClientHandlerStartListener;

public class SimpleClientHandler implements Runnable {


  protected SimpleClient client;
  protected SimpleServer server;
  protected PrintWriter writer;
  protected BufferedReader reader;
  private boolean running, started;
  private Thread handlerThread;
  private Gson json;

  private List<SimpleClientHandlerMessagesListener> messagesListeners = new ArrayList<>();
  private List<SimpleClientHandlerStartListener> startListeners = new ArrayList<>();
  private List<SimpleClientHandlerDisconnectListener> disconnectListeners = new ArrayList<>();
  private List<SimpleClientHandlerExceptionListener> exceptionListeners = new ArrayList<>();


  @Override
  public void run() {

    json = new Gson();

    while (running) {
      try {

        String message = "";
        while ((message = reader.readLine()) != null) {

          @SuppressWarnings("unchecked")
          Message messageObj = new Message((Map<String, Object>) json.fromJson(message, Map.class));

          onReceiveMessage(messageObj);
        }
      } catch (IOException e) {

        onReceiveMessageException(e);
      }
    }
  }

  public void sendMessage(Message message) {

    if (running) {

      onBeforeSendMessage(message);

      writer.println(message.json());
      writer.flush();

      onAfterSendMessage(message);
    }
  }

  public void start() {
    if (!started) {

      onBeforeStart();

      started = true;

      try {

        writer = new PrintWriter(client.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        running = true;

        handlerThread = new Thread(this);
        handlerThread.start();

      } catch (IOException e) {

        onStartException(e);
      }

      onAfterStart();
    }
  }

  public void disconnect() {

    if (started) {

      onBeforeDisconnect();

      started = false;
      running = false;
      if (handlerThread != null)
        handlerThread.interrupt();
      handlerThread = null;

      try {

        reader.close();
      } catch (IOException e) {
        onReaderCloseException(e, reader);
      }

      writer.close();

      try {

        client.close();
        this.server.getClientHandlers().remove(this.client.getInetAddress().getHostAddress());
        this.server.getClientHandlers().remove(this.client.getInetAddress().getHostName());

      } catch (IOException e) {
        onHandlerCloseException(e);
      }

      reader = null;
      writer = null;
      client = null;

      onAfterDisconnect();
    }
  }

  public void useDefaultListener() {

    addCompleteListener(new SimpleClientHandlerDefaultListener(server));
  }


  public List<SimpleClientHandlerMessagesListener> getMessagesListeners() {
    return messagesListeners;
  }

  public List<SimpleClientHandlerStartListener> getStartListeners() {
    return startListeners;
  }

  public List<SimpleClientHandlerDisconnectListener> getDisconnectListeners() {
    return disconnectListeners;
  }

  public List<SimpleClientHandlerExceptionListener> getExceptionListeners() {
    return exceptionListeners;
  }

  public void addCompleteListener(SimpleClientHandlerCompleteListener listener) {

    this.messagesListeners.add(listener);
    this.startListeners.add(listener);
    this.disconnectListeners.add(listener);
    this.exceptionListeners.add(listener);
  }

  public void addMessagesListener(SimpleClientHandlerMessagesListener listener) {

    this.messagesListeners.add(listener);
  }

  public void addStartListener(SimpleClientHandlerStartListener listener) {

    this.startListeners.add(listener);
  }

  public void addDisconnectListener(SimpleClientHandlerDisconnectListener listener) {

    this.disconnectListeners.add(listener);
  }

  public void addExceptionListener(SimpleClientHandlerExceptionListener listener) {

    this.exceptionListeners.add(listener);
  }

  private void onReceiveMessage(Message message) {

    for (SimpleClientHandlerMessagesListener listener : this.messagesListeners)
      listener.receiveMessage(message, this.client);
  }

  private void onBeforeSendMessage(Message message) {

    for (SimpleClientHandlerMessagesListener listener : this.messagesListeners)
      listener.beforeSendMessage(message, this.client);
  }

  private void onAfterSendMessage(Message message) {

    for (SimpleClientHandlerMessagesListener listener : this.messagesListeners)
      listener.afterSendMessage(message, this.client);
  }

  private void onBeforeStart() {

    for (SimpleClientHandlerStartListener listener : this.startListeners)
      listener.beforeStart(this);
  }

  private void onAfterStart() {

    for (SimpleClientHandlerStartListener listener : this.startListeners)
      listener.afterStart(this);
  }

  private void onBeforeDisconnect() {

    for (SimpleClientHandlerDisconnectListener listener : this.disconnectListeners)
      listener.beforeDisconnect(this);
  }

  private void onAfterDisconnect() {

    for (SimpleClientHandlerDisconnectListener listener : this.disconnectListeners)
      listener.afterDisconnect(this);
  }

  private void onReceiveMessageException(IOException e) {

    for (SimpleClientHandlerExceptionListener listener : this.exceptionListeners)
      listener.receiveMessageException(e);
  }

  private void onStartException(IOException e) {

    for (SimpleClientHandlerExceptionListener listener : this.exceptionListeners)
      listener.startException(e, this);
  }

  private void onReaderCloseException(IOException e, BufferedReader reader) {

    for (SimpleClientHandlerExceptionListener listener : this.exceptionListeners)
      listener.readerCloseException(e, reader);
  }

  private void onHandlerCloseException(IOException e) {

    for (SimpleClientHandlerExceptionListener listener : this.exceptionListeners)
      listener.handlerCloseException(e, this);
  }


  public boolean isStarted() {

    return this.started;
  }

  public boolean isRunning() {

    return this.running;
  }

  public SimpleClient getClient() {

    return this.client;
  }


  public SimpleClientHandler(SimpleClient client, SimpleServer server) {

    this.client = client;
    this.server = server;
  }
}
