package hava.socket.simple.common.message;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Message extends HashMap<String, Object> {

  private static final long serialVersionUID = 1L;

  @Override
  public Object get(Object key) {

    return super.get(key);
  }

  public Message(String message) {

    super.put("content", message);
  }

  public Message(Map<String, Object> message) {

    for (Entry<String, Object> entrada : message.entrySet()) {

      super.put(entrada.getKey(), entrada.getValue());
    }
  }

  public String json() {

    return new Gson().toJson(this);
  }

  public String prettyJson() {

    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
