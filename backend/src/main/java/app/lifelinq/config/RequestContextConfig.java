package app.lifelinq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import app.lifelinq.features.household.application.HouseholdApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RequestContextConfig {

    @Bean(name = "lifeLinqRequestContextFilter")
    public RequestContextFilter lifeLinqRequestContextFilter(
            JwtVerifier jwtVerifier,
            HouseholdApplicationService householdApplicationService
    ) {
        return new RequestContextFilter(jwtVerifier, householdApplicationService);
    }

    @Bean
    public JwtVerifier jwtVerifier(@Value("${lifelinq.jwt.secret:dev-secret}") String secret) {
        return new JwtVerifier(secret);
    }

    @Bean
    public JwtSigner jwtSigner(
            @Value("${lifelinq.jwt.secret:dev-secret}") String secret,
            @Value("${lifelinq.jwt.ttlSeconds:900}") long ttlSeconds,
            @Value("${lifelinq.jwt.issuer:}") String issuer,
            @Value("${lifelinq.jwt.audience:}") String audience
    ) {
        return new JwtSigner(secret, ttlSeconds, issuer, audience, java.time.Clock.systemUTC());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
