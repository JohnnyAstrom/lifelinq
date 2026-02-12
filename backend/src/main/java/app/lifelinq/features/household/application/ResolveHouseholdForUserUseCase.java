package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ResolveHouseholdForUserUseCase {
    private final MembershipRepository membershipRepository;

    public ResolveHouseholdForUserUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public Optional<UUID> resolveForUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<UUID> householdIds = membershipRepository.findHouseholdIdsByUserId(userId);
        if (householdIds.size() == 1) {
            return Optional.of(householdIds.get(0));
        }
        if (householdIds.size() > 1) {
            throw new AmbiguousHouseholdException("Multiple households for user");
        }
        return Optional.empty();
    }
}
