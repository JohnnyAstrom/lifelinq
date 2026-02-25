package app.lifelinq.features.group.domain;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository {
    void save(Group group);

    Optional<Group> findById(UUID id);

    void deleteById(UUID id);
}
