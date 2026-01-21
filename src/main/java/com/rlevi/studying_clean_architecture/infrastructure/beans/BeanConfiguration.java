package com.rlevi.studying_clean_architecture.infrastructure.beans;

import com.rlevi.studying_clean_architecture.core.gateway.TokenGateway;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.deleteuser.DeleteUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.deleteuser.DeleteUserUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.findallusers.FindAllUsersUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.findallusers.FindAllUsersUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail.FindUserByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail.FindUserByEmailUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyid.FindUserByIdUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyid.FindUserByIdUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.updateuser.UpdateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.updateuser.UpdateUserUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.verifyexistsbyemail.VerifyExistsByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.verifyexistsbyemail.VerifyExistsByEmailUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.loginuser.LoginUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.loginuser.LoginUserUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.usecases.refreshtoken.RefreshTokenUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.refreshtoken.RefreshTokenUseCaseImpl;
import com.rlevi.studying_clean_architecture.core.gateway.RefreshTokenGateway;
import com.rlevi.studying_clean_architecture.infrastructure.gateway.BCryptPasswordEncoderGateway;
import com.rlevi.studying_clean_architecture.infrastructure.security.CustomUserDetailsService;
import com.rlevi.studying_clean_architecture.infrastructure.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfiguration {

  @Bean
  public CreateUserUseCase createUserUseCase(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway, TokenGateway tokenGateway, RefreshTokenGateway refreshTokenGateway) {
    return new CreateUserUseCaseImpl(userGateway, passwordEncoderGateway, tokenGateway, refreshTokenGateway);
  }

  @Bean
  public LoginUserUseCase loginUserUseCase(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway, RefreshTokenGateway refreshTokenGateway, TokenGateway tokenGateway) {
    return new LoginUserUseCaseImpl(userGateway, passwordEncoderGateway, refreshTokenGateway, tokenGateway);
  }

  @Bean
  public UpdateUserUseCase updateUserUseCase(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    return new UpdateUserUseCaseImpl(userGateway, passwordEncoderGateway);
  }

  @Bean
  public FindUserByIdUseCase findUserByIdUseCase(UserGateway userGateway) {
    return new FindUserByIdUseCaseImpl(userGateway);
  }

  @Bean
  public FindUserByEmailUseCase findUserByEmailUseCase(UserGateway userGateway) {
    return new FindUserByEmailUseCaseImpl(userGateway);
  }

  @Bean
  public FindAllUsersUseCase findAllUsersUseCase(UserGateway userGateway) {
    return new FindAllUsersUseCaseImpl(userGateway);
  }

  @Bean
  public VerifyExistsByEmailUseCase verifyExistsByEmailUseCase(UserGateway userGateway) {
    return new VerifyExistsByEmailUseCaseImpl(userGateway);
  }

  @Bean
  public DeleteUserUseCase deleteUserUseCase(UserGateway userGateway) {
    return new DeleteUserUseCaseImpl(userGateway);
  }

  @Bean
  public RefreshTokenUseCase refreshTokenUseCase(UserGateway userGateway, TokenGateway tokenGateway, RefreshTokenGateway refreshTokenGateway) {
    return new RefreshTokenUseCaseImpl(userGateway, tokenGateway, refreshTokenGateway);
  }

  @Bean
  public PasswordEncoderGateway passwordEncoderGateway(PasswordEncoder passwordEncoder) {
    return new BCryptPasswordEncoderGateway(passwordEncoder);
  }

  @Bean
  public UserDetailsService userDetailsService(UserGateway userGateway) {
    return new CustomUserDetailsService(userGateway);
  }
}
