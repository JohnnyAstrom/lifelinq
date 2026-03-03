package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthPersistenceConfig {

    @Bean
    public AuthIdentityMapper authIdentityMapper() {
        return new AuthIdentityMapper();
    }

    @Bean
    public MagicLinkChallengeMapper magicLinkChallengeMapper() {
        return new MagicLinkChallengeMapper();
    }

    @Bean
    public JpaAuthIdentityRepositoryAdapter authIdentityRepository(
            AuthIdentityJpaRepository repository,
            AuthIdentityMapper mapper
    ) {
        return new JpaAuthIdentityRepositoryAdapter(repository, mapper);
    }

    @Bean
    public JpaMagicLinkChallengeRepositoryAdapter magicLinkChallengeRepository(
            MagicLinkChallengeJpaRepository repository,
            MagicLinkChallengeMapper mapper
    ) {
        return new JpaMagicLinkChallengeRepositoryAdapter(repository, mapper);
    }

    @Bean
    public MagicLinkTokenGenerator magicLinkTokenGenerator() {
        return new SecureMagicLinkTokenGenerator();
    }

    @Bean
    public AuthMailSender authMailSender() {
        return new DevAuthMailSender();
    }
}

