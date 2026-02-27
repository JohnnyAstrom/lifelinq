package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.infrastructure.UserDefaultGroupProvisioningAdapter;
import app.lifelinq.features.user.contract.UserProvisioning;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProfileRead;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupApplicationConfig {

    @Bean
    public CreateGroupUseCase createGroupUseCase(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository
    ) {
        return new CreateGroupUseCase(groupRepository, membershipRepository);
    }

    @Bean
    public AddMemberToGroupUseCase addMemberToGroupUseCase(
            MembershipRepository membershipRepository
    ) {
        return new AddMemberToGroupUseCase(membershipRepository);
    }

    @Bean
    public ListGroupMembersUseCase listGroupMembersUseCase(
            MembershipRepository membershipRepository
    ) {
        return new ListGroupMembersUseCase(membershipRepository);
    }

    @Bean
    public RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase(
            MembershipRepository membershipRepository
    ) {
        return new RemoveMemberFromGroupUseCase(membershipRepository);
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
    public GroupApplicationService groupApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateGroupUseCase createGroupUseCase,
            AddMemberToGroupUseCase addMemberToGroupUseCase,
            ListGroupMembersUseCase listGroupMembersUseCase,
            RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase,
            CreateInvitationUseCase createInvitationUseCase,
            RevokeInvitationUseCase revokeInvitationUseCase,
            MembershipRepository membershipRepository,
            GroupRepository groupRepository,
            UserProvisioning userProvisioning,
            UserActiveGroupSelection userActiveGroupSelection,
            UserProfileRead userProfileRead,
            Clock clock
    ) {
        return new GroupApplicationService(
                acceptInvitationUseCase,
                createGroupUseCase,
                addMemberToGroupUseCase,
                listGroupMembersUseCase,
                removeMemberFromGroupUseCase,
                createInvitationUseCase,
                revokeInvitationUseCase,
                membershipRepository,
                groupRepository,
                userProvisioning,
                userActiveGroupSelection,
                userProfileRead,
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

    @Bean
    public EnsureGroupMemberUseCase ensureGroupMemberUseCase(
            MembershipRepository membershipRepository
    ) {
        return new EnsureGroupMemberUseCaseImpl(membershipRepository);
    }

    @Bean
    public UserDefaultGroupProvisioning userDefaultGroupProvisioning(
            GroupApplicationService groupApplicationService
    ) {
        return new UserDefaultGroupProvisioningAdapter(groupApplicationService);
    }
}
