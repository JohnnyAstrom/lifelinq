package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.contract.CreateInvitationOutput;
import app.lifelinq.features.household.infrastructure.HouseholdJpaRepository;
import app.lifelinq.features.household.infrastructure.InvitationJpaRepository;
import app.lifelinq.features.household.infrastructure.MembershipJpaRepository;
import app.lifelinq.features.household.infrastructure.HouseholdMapper;
import app.lifelinq.features.household.infrastructure.InvitationMapper;
import app.lifelinq.features.household.infrastructure.MembershipMapper;
import app.lifelinq.features.household.infrastructure.JpaHouseholdRepositoryAdapter;
import app.lifelinq.features.household.infrastructure.JpaInvitationRepositoryAdapter;
import app.lifelinq.features.household.infrastructure.JpaMembershipRepositoryAdapter;
import app.lifelinq.features.household.infrastructure.InMemoryInvitationTokenGenerator;
import app.lifelinq.features.household.infrastructure.InvitationFlowTestApplication;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = InvitationFlowTestApplication.class)
@ActiveProfiles("test")
class InvitationFlowIntegrationTest {

    @Autowired
    private HouseholdJpaRepository householdJpaRepository;

    @Autowired
    private MembershipJpaRepository membershipJpaRepository;

    @Autowired
    private InvitationJpaRepository invitationJpaRepository;

    private HouseholdApplicationService householdApplicationService;

    @BeforeEach
    void setUp() {
        HouseholdRepository householdRepository = new JpaHouseholdRepositoryAdapter(
                householdJpaRepository,
                new HouseholdMapper()
        );
        MembershipRepository membershipRepository = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        InvitationRepository invitationRepository = new JpaInvitationRepositoryAdapter(
                invitationJpaRepository,
                new InvitationMapper()
        );
        UserRepository userRepository = new FakeUserRepository();
        EnsureUserExistsUseCase ensureUserExistsUseCase = new EnsureUserExistsUseCase(userRepository);

        CreateHouseholdUseCase createHouseholdUseCase = new CreateHouseholdUseCase(
                householdRepository,
                membershipRepository
        );
        AddMemberToHouseholdUseCase addMemberToHouseholdUseCase = new AddMemberToHouseholdUseCase(membershipRepository);
        ListHouseholdMembersUseCase listHouseholdMembersUseCase = new ListHouseholdMembersUseCase(membershipRepository);
        RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase = new RemoveMemberFromHouseholdUseCase(membershipRepository);
        CreateInvitationUseCase createInvitationUseCase = new CreateInvitationUseCase(
                invitationRepository,
                new InMemoryInvitationTokenGenerator()
        );
        AcceptInvitationUseCase acceptInvitationUseCase = new AcceptInvitationUseCase(
                invitationRepository,
                membershipRepository
        );
        RevokeInvitationUseCase revokeInvitationUseCase = new RevokeInvitationUseCase(invitationRepository);

        Clock clock = Clock.fixed(Instant.parse("2026-02-12T00:00:00Z"), ZoneOffset.UTC);

        householdApplicationService = new HouseholdApplicationService(
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

    private static final class FakeUserRepository implements UserRepository {
        private final java.util.Map<UUID, User> users = new java.util.HashMap<>();

        @Override
        public java.util.Optional<User> findById(UUID id) {
            return java.util.Optional.ofNullable(users.get(id));
        }

        @Override
        public void save(User user) {
            users.put(user.getId(), user);
        }
    }

    @Test
    void createAcceptInvitationCreatesMembership() {
        UUID ownerUserId = UUID.randomUUID();
        UUID invitedUserId = UUID.randomUUID();

        UUID householdId = householdApplicationService.createHousehold("Home", ownerUserId);

        CreateInvitationOutput output = householdApplicationService.createInvitation(
                householdId,
                ownerUserId,
                "Invitee@Example.com",
                Duration.ofDays(3)
        );

        householdApplicationService.acceptInvitation(output.token(), invitedUserId);

        List<Membership> memberships = householdApplicationService.listMembers(householdId);
        assertEquals(2, memberships.size());
        assertTrue(memberships.stream().anyMatch(m -> m.getUserId().equals(ownerUserId)
                && m.getRole() == HouseholdRole.OWNER));
        assertTrue(memberships.stream().anyMatch(m -> m.getUserId().equals(invitedUserId)
                && m.getRole() == HouseholdRole.MEMBER));
    }
}
