package app.lifelinq.features.shopping.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingListJpaRepository extends JpaRepository<ShoppingListEntity, UUID> {
    @Override
    @EntityGraph(attributePaths = "items")
    Optional<ShoppingListEntity> findById(UUID id);

    @EntityGraph(attributePaths = "items")
    List<ShoppingListEntity> findByGroupId(UUID groupId);
}
