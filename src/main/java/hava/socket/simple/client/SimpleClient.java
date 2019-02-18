package hava.socket.simple.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import hava.socket.simple.client.listeners.SimpleClientCompleteListener;
import hava.socket.simple.client.listeners.SimpleClientDefaultListener;
import hava.socket.simple.client.listeners.SimpleClientDisconnectListener;
import hava.socket.simple.client.listeners.SimpleClientExceptionListener;
import hava.socket.simple.client.listeners.SimpleClientMessagesListener;
import hava.socket.simple.client.listeners.SimpleClientStartListener;
import hava.socket.simple.common.message.Message;

public class SimpleClient extends Socket implements Runnable {


  protected PrintWriter writer;
  protected BufferedReader reader;
  private boolean running;
  private Thread clientThread;

  private List<SimpleClientMessagesListener> messagesListeners = new ArrayList<>();
  private List<SimpleClientDisconnectListener> disconnectListeners = new ArrayList<>();
  private List<SimpleClientStartListener> startListeners = new ArrayList<>();
  private List<SimpleClientExceptionListener> exceptionListeners = new ArrayList<>();


  @Override
  public void run() {

    Gson json = new Gson();

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

    if (!running) {

      onBeforeStart();

      try {

        writer = new PrintWriter(getOutputStream());
        reader = new BufferedReader(new InputStreamReader(getInputStream()));

        running = true;

        clientThread = new Thread(this);
        clientThread.start();
      } catch (IOException e) {

        onStartException(e);
      }

      onAfterStart();
    }
  }

  public void disconnect() {

    if (running) {

      onBeforeDisconnect();

      running = false;

      if (clientThread != null)
        clientThread.interrupt();
      clientThread = null;

      try {
        reader.close();
      } catch (IOException e) {
        onReaderCloseException(e, reader);
      }

      writer.close();

      try {
        close();
      } catch (IOException e) {
        onClientCloseException(e);
      }

      reader = null;
      writer = null;

      onAfterDisconnect();
    }
  }

  public void useDefaultListener() {

    addCompleteListener(new SimpleClientDefaultListener());
  }


  public List<SimpleClientMessagesListener> getMessagesListeners() {
    return messagesListeners;
  }

  public List<SimpleClientDisconnectListener> getDisconnectListeners() {
    return disconnectListeners;
  }

  public List<SimpleClientStartListener> getStartListeners() {
    return startListeners;
  }

  public List<SimpleClientExceptionListener> getExceptionListeners() {
    return exceptionListeners;
  }

  public void addCompleteListener(SimpleClientCompleteListener listener) {

    this.messagesListeners.add(listener);
    this.startListeners.add(listener);
    this.disconnectListeners.add(listener);
  }

  public void addMessagesListener(SimpleClientMessagesListener listener) {

    this.messagesListeners.add(listener);
  }

  public void addOnStartListener(SimpleClientStartListener listener) {

    this.startListeners.add(listener);
  }

  public void addOnDisconnectionListener(SimpleClientDisconnectListener listener) {

    this.disconnectListeners.add(listener);
  }

  public void addOnExceptionListener(SimpleClientExceptionListener listener) {

    this.exceptionListeners.add(listener);
  }

  private void onReceiveMessage(Message message) {

    for (SimpleClientMessagesListener listener : this.messagesListeners)
      listener.receiveMessage(message);
  }

  private void onBeforeSendMessage(Message message) {

    for (SimpleClientMessagesListener listener : this.messagesListeners)
      listener.beforeSendMessage(message);
  }

  private void onAfterSendMessage(Message message) {

    for (SimpleClientMessagesListener listener : this.messagesListeners)
      listener.afterSendMessage(message);
  }

  private void onBeforeStart() {

    for (SimpleClientStartListener listener : this.startListeners)
      listener.beforeStart(this);
  }

  private void onAfterStart() {

    for (SimpleClientStartListener listener : this.startListeners)
      listener.afterStart(this);
  }

  private void onBeforeDisconnect() {

    for (SimpleClientDisconnectListener listener : this.disconnectListeners)
      listener.beforeDisconnect(this);
  }

  private void onAfterDisconnect() {

    for (SimpleClientDisconnectListener listener : this.disconnectListeners)
      listener.afterDisconnect(this);
  }

  private void onReceiveMessageException(IOException e) {

    for (SimpleClientExceptionListener listener : this.exceptionListeners)
      listener.receiveMessageException(e);
  }

  private void onStartException(IOException e) {

    for (SimpleClientExceptionListener listener : this.exceptionListeners)
      listener.startException(e, this);
  }

  private void onReaderCloseException(IOException e, BufferedReader reader) {

    for (SimpleClientExceptionListener listener : this.exceptionListeners)
      listener.readerCloseException(e, reader);
  }

  private void onClientCloseException(IOException e) {

    for (SimpleClientExceptionListener listener : this.exceptionListeners)
      listener.clientCloseException(e, this);
  }


  public SimpleClient(String host, int port) throws UnknownHostException, IOException {

    super(host, port);
  }

  public SimpleClient(SocketImpl impl) throws SocketException {

    super(impl);
  }
}
