package com.example.digitaltwin;

import kalix.springsdk.KalixConfiguration;
import kalix.springsdk.annotations.Acl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(KalixConfiguration.class)
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting Kalix - Spring SDK");
    SpringApplication.run(Main.class, args);
  }
}