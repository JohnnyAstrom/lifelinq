package app.lifelinq.features.user.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = UserJpaTestApplication.class)
@ActiveProfiles("test")
class JpaUserRepositoryAdapterTest {

    @Autowired
    private UserRepository repository;

    @Test
    void savesAndLoadsUserRoundTrip() {
        User user = new User(UUID.randomUUID());

        repository.save(user);

        Optional<User> loaded = repository.findById(user.getId());
        assertTrue(loaded.isPresent());
        assertEquals(user.getId(), loaded.get().getId());
    }
}
