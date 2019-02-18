package com.sun.tools.javac.code;

import java.lang.String;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/teste")
public class ClassSymbol {
  @GetMapping
  @ResponseBody
  public String all() {
    return "Teste";
  }
}
