package app.lifelinq.features.user.application;

import app.lifelinq.features.user.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserApplicationConfig {

    @Bean
    public EnsureUserExistsUseCase ensureUserExistsUseCase(UserRepository userRepository) {
        return new EnsureUserExistsUseCase(userRepository);
    }

    @Bean
    public UserApplicationService userApplicationService(EnsureUserExistsUseCase ensureUserExistsUseCase) {
        return new UserApplicationService(ensureUserExistsUseCase);
    }
}
