package app.lifelinq.features.group.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.List;
import java.util.UUID;

final class EnsureGroupMemberUseCaseImpl implements EnsureGroupMemberUseCase {
    private final MembershipRepository membershipRepository;

    EnsureGroupMemberUseCaseImpl(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    @Override
    public void execute(UUID groupId, UUID actorUserId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (actorUserId == null) {
            throw new IllegalArgumentException("actorUserId must not be null");
        }
        List<Membership> memberships = membershipRepository.findByGroupId(groupId);
        for (Membership membership : memberships) {
            if (membership.getUserId().equals(actorUserId)) {
                return;
            }
        }
        throw new AccessDeniedException("Actor is not a member of the group");
    }
}
