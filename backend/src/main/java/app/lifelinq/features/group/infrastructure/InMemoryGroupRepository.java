package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryGroupRepository implements GroupRepository {
    private final Map<UUID, Group> groups = new HashMap<>();

    @Override
    public void save(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        groups.put(group.getId(), group);
    }

    @Override
    public Optional<Group> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(groups.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        groups.remove(id);
    }
}
