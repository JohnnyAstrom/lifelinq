package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.MembershipRepository;

final class GroupUseCases {
    private final CreateGroupUseCase createGroupUseCase;
    private final AddMemberToGroupUseCase addMemberToGroupUseCase;
    private final ListGroupMembersUseCase listGroupMembersUseCase;
    private final RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase;
    private final CreateInvitationUseCase createInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final ExpireInvitationsUseCase expireInvitationsUseCase;

    public GroupUseCases(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator
    ) {
        if (groupRepository == null) {
            throw new IllegalArgumentException("groupRepository must not be null");
        }
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        if (tokenGenerator == null) {
            throw new IllegalArgumentException("tokenGenerator must not be null");
        }
        this.createGroupUseCase = new CreateGroupUseCase(groupRepository, membershipRepository);
        this.addMemberToGroupUseCase = new AddMemberToGroupUseCase(membershipRepository);
        this.listGroupMembersUseCase = new ListGroupMembersUseCase(membershipRepository);
        this.removeMemberFromGroupUseCase = new RemoveMemberFromGroupUseCase(membershipRepository);
        this.createInvitationUseCase = new CreateInvitationUseCase(invitationRepository, tokenGenerator);
        this.acceptInvitationUseCase = new AcceptInvitationUseCase(invitationRepository, membershipRepository);
        this.expireInvitationsUseCase = new ExpireInvitationsUseCase(invitationRepository);
    }

    public CreateGroupUseCase createGroup() {
        return createGroupUseCase;
    }

    public AddMemberToGroupUseCase addMember() {
        return addMemberToGroupUseCase;
    }

    public ListGroupMembersUseCase listMembers() {
        return listGroupMembersUseCase;
    }

    public RemoveMemberFromGroupUseCase removeMember() {
        return removeMemberFromGroupUseCase;
    }

    public CreateInvitationUseCase createInvitation() {
        return createInvitationUseCase;
    }

    public AcceptInvitationUseCase acceptInvitation() {
        return acceptInvitationUseCase;
    }

    public ExpireInvitationsUseCase expireInvitations() {
        return expireInvitationsUseCase;
    }
}
