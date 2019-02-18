package hava.socket.simple.client.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.common.message.Message;

public class SimpleClientDefaultListener implements SimpleClientCompleteListener {


  public SimpleClientDefaultListener() {}

  @Override
  public void beforeStart(SimpleClient client) {

    System.out.println("Iniciando cliente...");
  }

  @Override
  public void afterStart(SimpleClient client) {

    System.out.println("Cliente iniciado com sucesso!");
  }

  @Override
  public void receiveMessage(Message message) {

    System.out.println(String.format("Mensagem recebida: %s", message.prettyJson()));
  }

  @Override
  public void beforeSendMessage(Message message) {

    System.out.println("Enviando mensagem...");
  }

  @Override
  public void afterSendMessage(Message message) {

    System.out.println("Mensagem enviada com sucesso!");
  }

  @Override
  public void beforeDisconnect(SimpleClient client) {

    System.out.println("Desconectando cliente...");
  }

  @Override
  public void afterDisconnect(SimpleClient client) {

    System.out.println("Cliente desconectado com sucesso!");
  }

  @Override
  public void receiveMessageException(IOException e) {

    System.out.println(String.format("Houve um erro ao receber uma mensagem: %s", e.getMessage()));
  }

  @Override
  public void startException(IOException e, SimpleClient client) {

    System.out.println(String.format("Houve um erro ao iniciar o cliente: %s", e.getMessage()));
  }

  @Override
  public void readerCloseException(IOException e, BufferedReader reader) {

    System.out.println(
        String.format("Houve um erro ao fechar o leitor de mensagens: %s", e.getMessage()));
  }

  @Override
  public void clientCloseException(IOException e, SimpleClient client) {

    System.out.println(String.format("Houve um erro ao fechar o cliente: %s", e.getMessage()));
  }
}
