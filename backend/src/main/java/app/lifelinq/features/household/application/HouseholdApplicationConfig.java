package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.MembershipRepository;
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
            MembershipRepository membershipRepository
    ) {
        return new HouseholdApplicationService(
                acceptInvitationUseCase,
                createHouseholdUseCase,
                addMemberToHouseholdUseCase,
                listHouseholdMembersUseCase,
                removeMemberFromHouseholdUseCase,
                membershipRepository
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
}
