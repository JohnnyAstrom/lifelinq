package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.MembershipRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class HouseholdPersistenceConfig {

    @Bean
    public HouseholdMapper householdMapper() {
        return new HouseholdMapper();
    }

    @Bean
    public MembershipMapper membershipMapper() {
        return new MembershipMapper();
    }

    @Bean
    public InvitationMapper invitationMapper() {
        return new InvitationMapper();
    }

    @Bean
    public HouseholdRepository householdRepository(
            HouseholdJpaRepository householdJpaRepository,
            HouseholdMapper householdMapper
    ) {
        return new JpaHouseholdRepositoryAdapter(householdJpaRepository, householdMapper);
    }

    @Bean
    public MembershipRepository membershipRepository(
            MembershipJpaRepository membershipJpaRepository,
            MembershipMapper membershipMapper
    ) {
        return new JpaMembershipRepositoryAdapter(membershipJpaRepository, membershipMapper);
    }

    @Bean
    public InvitationRepository invitationRepository(
            InvitationJpaRepository invitationJpaRepository,
            InvitationMapper invitationMapper
    ) {
        return new JpaInvitationRepositoryAdapter(invitationJpaRepository, invitationMapper);
    }
}
