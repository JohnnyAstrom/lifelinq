package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import java.util.Optional;
import java.util.UUID;

public final class JpaShoppingItemRepositoryAdapter implements ShoppingItemRepository {
    private final ShoppingItemJpaRepository repository;
    private final ShoppingItemMapper mapper;

    public JpaShoppingItemRepositoryAdapter(
            ShoppingItemJpaRepository repository,
            ShoppingItemMapper mapper
    ) {
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
    public void save(ShoppingItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        repository.save(mapper.toEntity(item));
    }

    @Override
    public Optional<ShoppingItem> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return repository.findById(id).map(mapper::toDomain);
    }
}
