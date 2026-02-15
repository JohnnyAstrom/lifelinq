package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.user.application.UserApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthApplicationConfig {

    @Bean
    public AuthApplicationService authApplicationService(
            UserApplicationService userApplicationService,
            JwtSigner jwtSigner
    ) {
        return new AuthApplicationService(userApplicationService, jwtSigner);
    }
}
