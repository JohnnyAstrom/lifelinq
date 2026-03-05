package app.lifelinq.features.auth.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.lifelinq.features.auth.domain.AuthMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {AuthPersistenceConfig.class, SmtpAuthMailSenderTest.TestConfig.class})
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "lifelinq.auth.refresh.secret=test-refresh-secret",
        "lifelinq.auth.mail.from=no-reply@lifelinq.dev",
        "spring.mail.username=smtp-user@lifelinq.dev"
})
class SmtpAuthMailSenderTest {

    @Autowired
    private AuthMailSender authMailSender;

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void authMailSenderBeanResolvesToSmtpAuthMailSenderInDevProfile() {
        assertInstanceOf(SmtpAuthMailSender.class, authMailSender);
    }

    @Test
    void sendMagicLinkUsesInjectedJavaMailSenderWithoutThrowing() {
        assertDoesNotThrow(() -> authMailSender.sendMagicLink("user@example.com", "http://localhost:8080/auth/magic/verify?token=abc"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Configuration
    static class TestConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }

        @Bean
        AuthIdentityJpaRepository authIdentityJpaRepository() {
            return mock(AuthIdentityJpaRepository.class);
        }

        @Bean
        MagicLinkChallengeJpaRepository magicLinkChallengeJpaRepository() {
            return mock(MagicLinkChallengeJpaRepository.class);
        }

        @Bean
        JpaRefreshSessionRepository jpaRefreshSessionRepository() {
            return mock(JpaRefreshSessionRepository.class);
        }

        @Bean
        JpaRefreshTokenRepository jpaRefreshTokenRepository() {
            return mock(JpaRefreshTokenRepository.class);
        }
    }
}
