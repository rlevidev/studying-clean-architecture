package com.rlevi.studying_clean_architecture.core.gateway;

public interface PasswordEncoderGateway {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}