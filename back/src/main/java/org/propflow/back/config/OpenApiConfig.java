package org.propflow.back.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI proposalsOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Proposals API")
                .description(
                    "REST API for the Proposals application: candidates, offices, positions, "
                        + "proposals, salaries, extras, tax brackets and reporting.")
                .version("v1"));
  }
}
