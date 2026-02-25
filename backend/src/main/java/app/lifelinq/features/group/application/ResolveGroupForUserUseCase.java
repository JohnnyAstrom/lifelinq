package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class ResolveGroupForUserUseCase {
    private final MembershipRepository membershipRepository;

    public ResolveGroupForUserUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public Optional<UUID> resolveForUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<UUID> groupIds = membershipRepository.findGroupIdsByUserId(userId);
        if (groupIds.size() == 1) {
            return Optional.of(groupIds.get(0));
        }
        if (groupIds.size() > 1) {
            throw new AmbiguousGroupException("Multiple groups for user");
        }
        return Optional.empty();
    }
}
