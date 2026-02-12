package app.lifelinq.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import app.lifelinq.features.household.application.ResolveHouseholdForUserUseCase;

@Configuration
public class RequestContextConfig {

    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter(
            JwtVerifier jwtVerifier,
            ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase
    ) {
        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestContextFilter(jwtVerifier, resolveHouseholdForUserUseCase));
        registration.setOrder(0);
        return registration;
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
}
