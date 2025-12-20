package com.rlevi.studying_clean_architecture.infrastructure.security;

import com.rlevi.studying_clean_architecture.infrastructure.exception.CustomAcessDeniedHandler;
import com.rlevi.studying_clean_architecture.infrastructure.exception.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Autowired
  private CustomAcessDeniedHandler customAcessDeniedHandler;

  @Autowired
  private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                    .frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin()))
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/v1/").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.accessDeniedHandler(customAcessDeniedHandler)
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
            );

    return http.build();
  }

  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
