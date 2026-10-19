package org.propflow.back;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class BackApplicationTests {

  @Autowired private ApplicationContext context;

  @Test
  void contextLoadsWithoutSeedingDemoData() {
    assertThat(context.getBeansOfType(CommandLineRunner.class)).isEmpty();
  }
}
