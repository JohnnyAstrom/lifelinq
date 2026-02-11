package app.lifelinq.features.shopping.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ShoppingItemJpaRepository extends JpaRepository<ShoppingItemEntity, UUID> {
}
