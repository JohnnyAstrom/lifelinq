package app.lifelinq.test;

import app.lifelinq.config.RequestContextConfig;
import app.lifelinq.config.SecurityConfig;
import app.lifelinq.features.auth.api.DevTokenController;
import app.lifelinq.features.auth.api.MeController;
import app.lifelinq.features.household.application.ResolveHouseholdForUserUseCase;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.household.infrastructure.InMemoryMembershipRepository;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({RequestContextConfig.class, SecurityConfig.class, DevTokenController.class, MeController.class})
public class DevAuthTestApplication {

    @Bean
    public MembershipRepository membershipRepository() {
        return new InMemoryMembershipRepository();
    }

    @Bean
    public ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase(
            MembershipRepository membershipRepository
    ) {
        return new ResolveHouseholdForUserUseCase(membershipRepository);
    }

    @Bean
    public UserRepository userRepository() {
        return new UserRepository() {
            private final Map<UUID, User> users = new ConcurrentHashMap<>();

            @Override
            public Optional<User> findById(UUID id) {
                return Optional.ofNullable(users.get(id));
            }

            @Override
            public void save(User user) {
                users.put(user.getId(), user);
            }
        };
    }

    @Bean
    public EnsureUserExistsUseCase ensureUserExistsUseCase(UserRepository userRepository) {
        return new EnsureUserExistsUseCase(userRepository);
    }
}
