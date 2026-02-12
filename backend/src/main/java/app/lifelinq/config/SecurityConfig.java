package app.lifelinq.config;

import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2LoginSuccessHandler successHandler
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth.successHandler(successHandler));
        return http.build();
    }

    @Bean
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler(
            EnsureUserExistsUseCase ensureUserExistsUseCase,
            JwtSigner jwtSigner,
            ObjectMapper objectMapper
    ) {
        return new OAuth2LoginSuccessHandler(ensureUserExistsUseCase, jwtSigner, objectMapper);
    }
}
