package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.user.contract.UserAccountDeletion;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProfileRead;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthApplicationConfig {

    @Bean
    public AuthApplicationService authApplicationService(
            UserProvisioning userProvisioning,
            UserAccountDeletion userAccountDeletion,
            UserActiveGroupSelection userActiveGroupSelection,
            UserActiveGroupRead userActiveGroupRead,
            UserProfileRead userProfileRead,
            UserDefaultGroupProvisioning userDefaultGroupProvisioning,
            UserGroupMembershipLookup userGroupMembershipLookup,
            AuthIdentityRepository authIdentityRepository,
            MagicLinkChallengeRepository magicLinkChallengeRepository,
            MagicLinkTokenGenerator magicLinkTokenGenerator,
            AuthMailSender authMailSender,
            JwtSigner jwtSigner,
            Clock clock,
            @Value("${lifelinq.auth.magic.ttlSeconds:900}") long magicLinkTtlSeconds,
            @Value("${lifelinq.auth.magic.verifyBaseUrl:http://localhost:8080/auth/magic/verify}") String magicVerifyBaseUrl,
            @Value("${lifelinq.auth.magic.completeBaseUrl:mobileapp://auth/complete}") String magicCompleteBaseUrl
    ) {
        return new AuthApplicationService(
                userProvisioning,
                userAccountDeletion,
                userActiveGroupSelection,
                userActiveGroupRead,
                userProfileRead,
                userDefaultGroupProvisioning,
                userGroupMembershipLookup,
                authIdentityRepository,
                magicLinkChallengeRepository,
                magicLinkTokenGenerator,
                authMailSender,
                jwtSigner,
                clock,
                Duration.ofSeconds(magicLinkTtlSeconds),
                magicVerifyBaseUrl,
                magicCompleteBaseUrl
        );
    }
}
