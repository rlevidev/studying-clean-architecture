package com.rlevi.studying_clean_architecture;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyingCleanArchitectureApplication {

  public static void main(String[] args) {
    // Load .env file
    Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    // Set environment variables from .env
    dotenv.entries().forEach(entry -> {
      if (System.getenv(entry.getKey()) == null) {
        System.setProperty(entry.getKey(), entry.getValue());
      }
    });

    SpringApplication.run(StudyingCleanArchitectureApplication.class, args);
  }

}
