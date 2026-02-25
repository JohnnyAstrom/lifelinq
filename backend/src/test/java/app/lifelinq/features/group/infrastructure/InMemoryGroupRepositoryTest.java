package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.Group;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryGroupRepositoryTest {

    @Test
    void requiresGroup() {
        InMemoryGroupRepository repository = new InMemoryGroupRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void savesGroup() {
        InMemoryGroupRepository repository = new InMemoryGroupRepository();
        Group group = new Group(UUID.randomUUID(), "Home");
        repository.save(group);
    }

    @Test
    void deletesById() {
        InMemoryGroupRepository repository = new InMemoryGroupRepository();
        Group group = new Group(UUID.randomUUID(), "Home");
        repository.save(group);

        repository.deleteById(group.getId());

        assertFalse(repository.findById(group.getId()).isPresent());
    }
}
