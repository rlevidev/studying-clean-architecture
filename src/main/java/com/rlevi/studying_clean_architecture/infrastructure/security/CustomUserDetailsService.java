package com.rlevi.studying_clean_architecture.infrastructure.security;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserGateway userGateway;

  public CustomUserDetailsService(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userGateway.findUserByEmail(email)
            .orElseThrow(() ->
                    new UsernameNotFoundException("User not found with email: " + email)
            );

    return org.springframework.security.core.userdetails.User.builder()
            .username(user.email())
            .password(user.passwordHash())
            .authorities("ROLE_" + user.role().name())
            .build();
  }
}
