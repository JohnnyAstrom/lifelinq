package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.user.application.UserApplicationServiceTestFactory;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupInMemoryWiringTest {

    @Test
    void createsApplicationService() {
        GroupApplicationService service = GroupInMemoryWiring.createApplicationService(
                UserApplicationServiceTestFactory.create(new InMemoryUserRepository())
        );

        assertNotNull(service);
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> storage = new HashMap<>();

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void save(User user) {
            storage.put(user.getId(), user);
        }

        @Override
        public void deleteById(UUID id) {
            storage.remove(id);
        }
    }
}
