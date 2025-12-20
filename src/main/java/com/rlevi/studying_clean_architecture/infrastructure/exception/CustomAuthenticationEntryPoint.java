package com.rlevi.studying_clean_architecture.infrastructure.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("error", "Not authenticated");
    responseBody.put("message", "You need to be logged in to access this feature.");
    responseBody.put("status", 401);

    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(responseBody));
  }
}
