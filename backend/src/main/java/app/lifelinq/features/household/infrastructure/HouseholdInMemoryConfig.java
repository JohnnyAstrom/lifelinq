package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.household.application.InvitationTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!persistence")
public class HouseholdInMemoryConfig {

    @Bean
    public HouseholdRepository householdRepository() {
        return new InMemoryHouseholdRepository();
    }

    @Bean
    public MembershipRepository membershipRepository() {
        return new InMemoryMembershipRepository();
    }

    @Bean
    public InvitationRepository invitationRepository() {
        return new InMemoryInvitationRepository();
    }

    @Bean
    public InvitationTokenGenerator invitationTokenGenerator() {
        return new InMemoryInvitationTokenGenerator();
    }
}
