package annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import hava.annotation.spring.annotations.Authentication;
import hava.annotation.spring.annotations.HASConfiguration;

@SpringBootApplication
@HASConfiguration(debug = true)
@Authentication(secret = "GustavoLegal", encoder = NoOpPasswordEncoder.class)
public class Main {

  public static void main(String[] args) {
  
    SpringApplication.run(Main.class, args);
  }
}