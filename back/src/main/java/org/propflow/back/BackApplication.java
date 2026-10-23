package org.propflow.back;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("USA/Eastern"));
    SpringApplication.run(BackApplication.class, args);
  }
}
