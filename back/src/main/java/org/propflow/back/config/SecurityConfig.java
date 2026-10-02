package org.propflow.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  // TODO: set up SecurityConfig properly for safe deployment
  @SuppressWarnings("deprecation")
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeRequests(authorizeRequests -> authorizeRequests
            .requestMatchers("/auth").permitAll()
            .requestMatchers("/").permitAll()
            .requestMatchers("/program/").hasRole("USER")
            .requestMatchers("/admin").hasRole("ADMIN")
            .anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable); // Disable CSRF
    return http.build();
  }
}
