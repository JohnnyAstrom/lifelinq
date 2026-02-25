package app.lifelinq.features.user.infrastructure;

import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.Optional;
import java.util.UUID;

public class JpaUserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository repository;
    private final UserMapper mapper;

    public JpaUserRepositoryAdapter(UserJpaRepository repository, UserMapper mapper) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void save(User user) {
        repository.save(mapper.toEntity(user));
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        repository.deleteById(id);
    }
}
