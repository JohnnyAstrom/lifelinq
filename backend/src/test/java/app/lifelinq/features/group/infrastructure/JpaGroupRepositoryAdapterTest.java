package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = GroupJpaTestApplication.class)
@ActiveProfiles("test")
class JpaGroupRepositoryAdapterTest {

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Test
    void savesAndLoadsRoundTrip() {
        JpaGroupRepositoryAdapter adapter = new JpaGroupRepositoryAdapter(
                groupJpaRepository,
                new GroupMapper()
        );
        Group group = new Group(UUID.randomUUID(), "Home");

        adapter.save(group);
        Optional<Group> loaded = adapter.findById(group.getId());

        assertTrue(loaded.isPresent());
        assertEquals(group.getId(), loaded.get().getId());
        assertEquals(group.getName(), loaded.get().getName());

        Membership membership = new Membership(loaded.get().getId(), UUID.randomUUID(), GroupRole.MEMBER);
        assertEquals(loaded.get().getId(), membership.getGroupId());
    }
}
