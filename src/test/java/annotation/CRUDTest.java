package annotation;

import hava.annotation.spring.annotations.Authentication;
import hava.annotation.spring.annotations.HASConfiguration;

@HASConfiguration(debug = true)
@Authentication(secret = "GustavoLegal", encoder = NoEncodingEncoder.class)
public class CRUDTest {

  public static void main(String[] args) {
  }
}