package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class ShoppingPersistenceConfig {

    @Bean
    public ShoppingItemMapper shoppingItemMapper() {
        return new ShoppingItemMapper();
    }

    @Bean
    public ShoppingItemRepository shoppingItemRepository(
            ShoppingItemJpaRepository repository,
            ShoppingItemMapper mapper
    ) {
        return new JpaShoppingItemRepositoryAdapter(repository, mapper);
    }
}
