package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.user.contract.UserAccountDeletion;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProvisioning;
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
            UserDefaultGroupProvisioning userDefaultGroupProvisioning,
            UserGroupMembershipLookup userGroupMembershipLookup,
            JwtSigner jwtSigner
    ) {
        return new AuthApplicationService(
                userProvisioning,
                userAccountDeletion,
                userActiveGroupSelection,
                userActiveGroupRead,
                userDefaultGroupProvisioning,
                userGroupMembershipLookup,
                jwtSigner
        );
    }
}
