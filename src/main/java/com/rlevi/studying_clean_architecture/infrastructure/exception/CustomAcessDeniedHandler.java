package com.rlevi.studying_clean_architecture.infrastructure.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAcessDeniedHandler implements AccessDeniedHandler {
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("error", "Access denied");
    responseBody.put("message", "You do not have permission to access this resource.");
    responseBody.put("path", request.getRequestURI());

    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(responseBody));
  }
}
