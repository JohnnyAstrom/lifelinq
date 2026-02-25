package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import java.util.Optional;
import java.util.UUID;

public final class JpaGroupRepositoryAdapter implements GroupRepository {
    private final GroupJpaRepository groupJpaRepository;
    private final GroupMapper mapper;

    public JpaGroupRepositoryAdapter(GroupJpaRepository groupJpaRepository, GroupMapper mapper) {
        if (groupJpaRepository == null) {
            throw new IllegalArgumentException("groupJpaRepository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.groupJpaRepository = groupJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Group group) {
        GroupEntity entity = mapper.toEntity(group);
        groupJpaRepository.save(entity);
    }

    @Override
    public Optional<Group> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return groupJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        groupJpaRepository.deleteById(id);
    }
}
