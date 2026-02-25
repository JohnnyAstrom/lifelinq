package app.lifelinq.features.user.application;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
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
    public DeleteAccountUseCase deleteAccountUseCase(
            GroupAccountDeletionGovernancePort groupAccountDeletionGovernancePort,
            UserRepository userRepository
    ) {
        return new DeleteAccountUseCase(groupAccountDeletionGovernancePort, userRepository);
    }

    @Bean
    public UserApplicationService userApplicationService(
            EnsureUserExistsUseCase ensureUserExistsUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            UserRepository userRepository
    ) {
        return new UserApplicationService(ensureUserExistsUseCase, deleteAccountUseCase, userRepository);
    }
}
