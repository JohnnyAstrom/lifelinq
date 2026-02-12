package app.lifelinq.config;

import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2LoginSuccessHandler successHandler,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository,
            RequestContextFilter requestContextFilter,
            @Value("${lifelinq.devAuth.enabled:false}") boolean devAuthEnabled
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/dev/token")
                        .access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(devAuthEnabled))
                        .anyRequest().authenticated()
                );

        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth.successHandler(successHandler));
        }
        http.addFilterBefore(requestContextFilter, UsernamePasswordAuthenticationFilter.class);
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
