package org.propflow.back;

import java.util.TimeZone;
import org.propflow.back.service.ReportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackApplication.class, args);
  }

  // Empties and recreates the PDF output folder on startup; tests set
  // app.reset-pdf-folder=false to skip it. Demo data is seeded by seed.DemoDataSeeder.
  @Bean
  @ConditionalOnProperty(name = "app.reset-pdf-folder", havingValue = "true", matchIfMissing = true)
  public CommandLineRunner resetPdfFolder(ReportService reportService) {
    return (args) -> {
      TimeZone.setDefault(TimeZone.getTimeZone("USA/Eastern"));
      reportService.deleteAll();
      reportService.init();
    };
  }
}
