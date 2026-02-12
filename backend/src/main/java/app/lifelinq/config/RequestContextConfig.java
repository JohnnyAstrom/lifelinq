package app.lifelinq.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;

@Configuration
public class RequestContextConfig {

    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter(
            JwtVerifier jwtVerifier,
            EnsureUserExistsUseCase ensureUserExistsUseCase
    ) {
        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestContextFilter(jwtVerifier, ensureUserExistsUseCase));
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public JwtVerifier jwtVerifier(@Value("${lifelinq.jwt.secret:dev-secret}") String secret) {
        return new JwtVerifier(secret);
    }
}
