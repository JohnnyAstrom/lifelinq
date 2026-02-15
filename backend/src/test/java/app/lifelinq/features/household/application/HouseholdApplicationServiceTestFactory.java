package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.user.application.UserApplicationServiceTestFactory;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class HouseholdApplicationServiceTestFactory {
    private HouseholdApplicationServiceTestFactory() {
    }

    public static HouseholdApplicationService createForContextResolution(MembershipRepository membershipRepository) {
        HouseholdRepository householdRepository = new StubHouseholdRepository();
        InvitationRepository invitationRepository = new StubInvitationRepository();
        InvitationTokenGenerator tokenGenerator = () -> "test-token";
        return HouseholdApplicationService.create(
                householdRepository,
                membershipRepository,
                invitationRepository,
                tokenGenerator,
                UserApplicationServiceTestFactory.create(new InMemoryUserRepository()),
                Clock.systemUTC()
        );
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final java.util.Map<UUID, User> users = new java.util.HashMap<>();

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public void save(User user) {
            users.put(user.getId(), user);
        }
    }

    private static final class StubHouseholdRepository implements HouseholdRepository {
        @Override
        public void save(Household household) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Household> findById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }
    }

    private static final class StubInvitationRepository implements InvitationRepository {
        @Override
        public Optional<Invitation> findByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean existsByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Invitation> findActive() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void save(Invitation invitation) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findActiveByHouseholdIdAndInviteeEmail(UUID householdId, String inviteeEmail) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
