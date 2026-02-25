package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.contract.CreateInvitationOutput;
import app.lifelinq.features.group.infrastructure.GroupJpaRepository;
import app.lifelinq.features.group.infrastructure.InvitationJpaRepository;
import app.lifelinq.features.group.infrastructure.MembershipJpaRepository;
import app.lifelinq.features.group.infrastructure.GroupMapper;
import app.lifelinq.features.group.infrastructure.InvitationMapper;
import app.lifelinq.features.group.infrastructure.MembershipMapper;
import app.lifelinq.features.group.infrastructure.JpaGroupRepositoryAdapter;
import app.lifelinq.features.group.infrastructure.JpaInvitationRepositoryAdapter;
import app.lifelinq.features.group.infrastructure.JpaMembershipRepositoryAdapter;
import app.lifelinq.features.group.infrastructure.InMemoryInvitationTokenGenerator;
import app.lifelinq.features.group.infrastructure.InvitationFlowTestApplication;
import app.lifelinq.features.user.application.UserApplicationService;
import app.lifelinq.features.user.application.UserApplicationServiceTestFactory;
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
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private MembershipJpaRepository membershipJpaRepository;

    @Autowired
    private InvitationJpaRepository invitationJpaRepository;

    private GroupApplicationService groupApplicationService;

    @BeforeEach
    void setUp() {
        GroupRepository groupRepository = new JpaGroupRepositoryAdapter(
                groupJpaRepository,
                new GroupMapper()
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
        UserApplicationService userApplicationService = UserApplicationServiceTestFactory.create(userRepository);

        CreateGroupUseCase createGroupUseCase = new CreateGroupUseCase(
                groupRepository,
                membershipRepository
        );
        AddMemberToGroupUseCase addMemberToGroupUseCase = new AddMemberToGroupUseCase(membershipRepository);
        ListGroupMembersUseCase listGroupMembersUseCase = new ListGroupMembersUseCase(membershipRepository);
        RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase = new RemoveMemberFromGroupUseCase(membershipRepository);
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

        ResolveGroupForUserUseCase resolveGroupForUserUseCase =
                new ResolveGroupForUserUseCase(membershipRepository);
        groupApplicationService = new GroupApplicationService(
                acceptInvitationUseCase,
                createGroupUseCase,
                addMemberToGroupUseCase,
                listGroupMembersUseCase,
                removeMemberFromGroupUseCase,
                createInvitationUseCase,
                revokeInvitationUseCase,
                membershipRepository,
                userApplicationService,
                resolveGroupForUserUseCase,
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

        UUID groupId = groupApplicationService.createGroup("Home", ownerUserId);

        CreateInvitationOutput output = groupApplicationService.createInvitation(
                groupId,
                ownerUserId,
                "Invitee@Example.com",
                Duration.ofDays(3)
        );

        groupApplicationService.acceptInvitation(output.token(), invitedUserId);

        List<Membership> memberships = groupApplicationService.listMembers(groupId);
        assertEquals(2, memberships.size());
        assertTrue(memberships.stream().anyMatch(m -> m.getUserId().equals(ownerUserId)
                && m.getRole() == GroupRole.OWNER));
        assertTrue(memberships.stream().anyMatch(m -> m.getUserId().equals(invitedUserId)
                && m.getRole() == GroupRole.MEMBER));
    }
}
