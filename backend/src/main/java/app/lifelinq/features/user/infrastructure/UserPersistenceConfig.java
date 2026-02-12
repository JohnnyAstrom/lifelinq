package app.lifelinq.features.user.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserPersistenceConfig {

    @Bean
    public UserMapper userMapper() {
        return new UserMapper();
    }

    @Bean
    public JpaUserRepositoryAdapter userRepository(UserJpaRepository repository, UserMapper mapper) {
        return new JpaUserRepositoryAdapter(repository, mapper);
    }
}
