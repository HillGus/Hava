package hava.socket.simple.server.handlers.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;
import hava.socket.simple.server.SimpleServer;
import hava.socket.simple.server.handlers.SimpleClientHandler;

public class SimpleClientHandlerDefaultListener implements SimpleClientHandlerCompleteListener {


  private SimpleServer server;
  private SimpleClientHandler clientHandler;


  public SimpleClientHandlerDefaultListener(SimpleServer server) {
    this.server = server;
  }

  @Override
  public void beforeStart(SimpleClientHandler clientHandler) {

    this.clientHandler = clientHandler;

    System.out.println("Iniciando conexão com novo cliente...");
  }

  @Override
  public void afterStart(SimpleClientHandler clientHandler) {

    System.out.println("Conexão com novo cliente iniciada com sucesso!");
  }

  @Override
  public void receiveMessage(Message message, SimpleClient client) {

    System.out.println(String.format("Mensagem recebida: %s", message.json()));
    server.sendMessageToAllFrom(message, client.getInetAddress().getHostAddress());
  }

  @Override
  public void beforeSendMessage(Message message, SimpleClient client) {

    System.out.println("Enviando mensagem...");
  }

  @Override
  public void afterSendMessage(Message message, SimpleClient client) {

    System.out.println("Mensagem enviada com sucesso!");
  }

  @Override
  public void beforeDisconnect(SimpleClientHandler clientHandler) {

    System.out.println("Desconectando cliente...");
  }

  @Override
  public void afterDisconnect(SimpleClientHandler clientHandler) {

    System.out.println("Cliente desconectado com sucesso!");
  }

  @Override
  public void receiveMessageException(IOException e) {

    if (e.getMessage().equals("Connection reset")) {

      System.out.println(String.format("O host %s desconectou do servidor",
          this.clientHandler.getClient().getInetAddress().getHostName()));
      this.clientHandler.disconnect();
      return;
    }

    System.out.println(String.format("Houve um erro ao receber uma mensagem: %s", e.getMessage()));
  }

  @Override
  public void startException(IOException e, SimpleClientHandler handler) {

    System.out.println(String.format("Houve um erro ao iniciar: %s", e.getMessage()));
    handler.disconnect();
  }

  @Override
  public void readerCloseException(IOException e, BufferedReader reader) {

    System.out.println(
        String.format("Houve um erro ao fechar o leitor de mensagens: %s", e.getMessage()));
  }

  @Override
  public void handlerCloseException(IOException e, SimpleClientHandler handler) {

    System.out.println(String.format("Houve um erro ao fechar a conexão: %s", e.getMessage()));
  }
}
