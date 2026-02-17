package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JpaShoppingListRepositoryAdapter implements ShoppingListRepository {
    private final ShoppingListJpaRepository repository;
    private final ShoppingListMapper mapper;

    public JpaShoppingListRepositoryAdapter(
            ShoppingListJpaRepository repository,
            ShoppingListMapper mapper
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
    public ShoppingList save(ShoppingList list) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        ShoppingListEntity saved = repository.save(mapper.toEntity(list));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ShoppingList> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ShoppingList> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<ShoppingList> result = new ArrayList<>();
        for (ShoppingListEntity entity : repository.findByHouseholdId(householdId)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        repository.deleteById(id);
    }
}
