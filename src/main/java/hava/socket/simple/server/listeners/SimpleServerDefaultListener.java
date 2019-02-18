package hava.socket.simple.server.listeners;

import java.io.IOException;
import hava.socket.simple.client.SimpleClient;
import hava.socket.simple.server.SimpleServer;
import hava.socket.simple.server.handlers.SimpleClientHandler;

public class SimpleServerDefaultListener implements SimpleServerCompleteListener {

  public SimpleServerDefaultListener() {}

  @Override
  public void beforeStart(SimpleServer server) {

    System.out.println("Iniciando servidor...");
  }

  @Override
  public void afterStart(SimpleServer server) {

    System.out.println("Servidor iniciado com sucesso!");
  }

  @Override
  public void clientConnect(SimpleClient client, SimpleClientHandler clientHandler) {

    clientHandler.useDefaultListener();
  }

  @Override
  public void beforeStop(SimpleServer server) {

    System.out.println("Desligando servidor...");
  }

  @Override
  public void afterStop(SimpleServer server) {

    System.out.println("Servidor desligado com sucesso!");
  }

  @Override
  public void clientConnectException(IOException e) {

    System.out.println(
        String.format("Houve um problema ao tentar conectar com um cliente: %s", e.getMessage()));
    e.printStackTrace();
  }

  @Override
  public void serverCloseException(IOException e, SimpleServer server) {

    System.out.println(
        String.format("Houve um problema ao tentar desligar o servidor: %s", e.getMessage()));
    e.printStackTrace();
  }

}
