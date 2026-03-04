package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
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
    public RefreshSessionRepository refreshSessionRepository(
            JpaRefreshSessionRepository repository
    ) {
        return new JpaRefreshSessionRepositoryAdapter(repository);
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository(
            JpaRefreshTokenRepository tokenRepository,
            JpaRefreshSessionRepository sessionRepository
    ) {
        return new JpaRefreshTokenRepositoryAdapter(tokenRepository, sessionRepository);
    }

    @Bean
    public MagicLinkTokenGenerator magicLinkTokenGenerator() {
        return new SecureMagicLinkTokenGenerator();
    }

    @Bean
    public RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRefreshTokenGenerator();
    }

    @Bean
    public RefreshTokenHasher refreshTokenHasher(
            @Value("${lifelinq.auth.refresh.secret}") String secret
    ) {
        return new HmacSha256RefreshTokenHasher(secret);
    }

    @Bean
    public AuthMailSender authMailSender() {
        return new DevAuthMailSender();
    }
}
