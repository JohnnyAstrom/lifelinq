package app.lifelinq.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@Profile("dev")
public class DevOAuthClientRegistrationConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${lifelinq.oauth.google.client-id:}") String googleClientId,
            @Value("${lifelinq.oauth.google.client-secret:}") String googleClientSecret,
            @Value("${lifelinq.oauth.apple.client-id:}") String appleClientId,
            @Value("${lifelinq.oauth.apple.client-secret:}") String appleClientSecret
    ) {
        List<ClientRegistration> registrations = new ArrayList<>();

        if (hasText(googleClientId) && hasText(googleClientSecret)) {
            registrations.add(
                    CommonOAuth2Provider.GOOGLE.getBuilder("google")
                            .clientId(googleClientId.trim())
                            .clientSecret(googleClientSecret.trim())
                            .scope("openid", "profile", "email")
                            .build()
            );
        }

        if (hasText(appleClientId) && hasText(appleClientSecret)) {
            registrations.add(ClientRegistration.withRegistrationId("apple")
                    .clientId(appleClientId.trim())
                    .clientSecret(appleClientSecret.trim())
                    .clientName("Apple")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "name", "email")
                    .authorizationUri("https://appleid.apple.com/auth/authorize")
                    .tokenUri("https://appleid.apple.com/auth/token")
                    .jwkSetUri("https://appleid.apple.com/auth/keys")
                    .userNameAttributeName("sub")
                    .build());
        }

        if (registrations.isEmpty()) {
            return registrationId -> null;
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
