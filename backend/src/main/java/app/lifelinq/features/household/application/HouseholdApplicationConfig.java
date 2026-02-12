package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HouseholdApplicationConfig {

    @Bean
    public CreateHouseholdUseCase createHouseholdUseCase(
            HouseholdRepository householdRepository,
            MembershipRepository membershipRepository
    ) {
        return new CreateHouseholdUseCase(householdRepository, membershipRepository);
    }

    @Bean
    public AddMemberToHouseholdUseCase addMemberToHouseholdUseCase(
            MembershipRepository membershipRepository
    ) {
        return new AddMemberToHouseholdUseCase(membershipRepository);
    }

    @Bean
    public ListHouseholdMembersUseCase listHouseholdMembersUseCase(
            MembershipRepository membershipRepository
    ) {
        return new ListHouseholdMembersUseCase(membershipRepository);
    }

    @Bean
    public ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase(
            MembershipRepository membershipRepository
    ) {
        return new ResolveHouseholdForUserUseCase(membershipRepository);
    }

    @Bean
    public RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase(
            MembershipRepository membershipRepository
    ) {
        return new RemoveMemberFromHouseholdUseCase(membershipRepository);
    }

    @Bean
    public CreateInvitationUseCase createInvitationUseCase(
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator
    ) {
        return new CreateInvitationUseCase(invitationRepository, tokenGenerator);
    }

    @Bean
    public AcceptInvitationUseCase acceptInvitationUseCase(
            InvitationRepository invitationRepository,
            MembershipRepository membershipRepository
    ) {
        return new AcceptInvitationUseCase(invitationRepository, membershipRepository);
    }

    @Bean
    public HouseholdApplicationService householdApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateHouseholdUseCase createHouseholdUseCase,
            AddMemberToHouseholdUseCase addMemberToHouseholdUseCase,
            ListHouseholdMembersUseCase listHouseholdMembersUseCase,
            RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase,
            CreateInvitationUseCase createInvitationUseCase,
            RevokeInvitationUseCase revokeInvitationUseCase,
            MembershipRepository membershipRepository,
            EnsureUserExistsUseCase ensureUserExistsUseCase,
            Clock clock
    ) {
        return new HouseholdApplicationService(
                acceptInvitationUseCase,
                createHouseholdUseCase,
                addMemberToHouseholdUseCase,
                listHouseholdMembersUseCase,
                removeMemberFromHouseholdUseCase,
                createInvitationUseCase,
                revokeInvitationUseCase,
                membershipRepository,
                ensureUserExistsUseCase,
                clock
        );
    }

    @Bean
    public ExpireInvitationsUseCase expireInvitationsUseCase(
            InvitationRepository invitationRepository
    ) {
        return new ExpireInvitationsUseCase(invitationRepository);
    }

    @Bean
    public RevokeInvitationUseCase revokeInvitationUseCase(
            InvitationRepository invitationRepository
    ) {
        return new RevokeInvitationUseCase(invitationRepository);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
