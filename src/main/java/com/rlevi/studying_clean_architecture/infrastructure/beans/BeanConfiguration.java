package com.rlevi.studying_clean_architecture.infrastructure.beans;

import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfiguration {

  @Bean
  public CreateUserUseCase createUserUseCase(UserGateway userGateway) {
    return new CreateUserUseCaseImpl(userGateway);
  }

  @Bean
  public UpdateUserUseCase updateUserUseCase(UserGateway userGateway, PasswordEncoder passwordEncoder) {
    return new UpdateUserUseCaseImpl(userGateway, passwordEncoder);
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
}
