package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class ShoppingPersistenceConfig {

    @Bean
    public ShoppingListMapper shoppingListMapper() {
        return new ShoppingListMapper();
    }

    @Bean
    public ShoppingListRepository shoppingListRepository(
            ShoppingListJpaRepository repository,
            ShoppingListMapper mapper
    ) {
        return new JpaShoppingListRepositoryAdapter(repository, mapper);
    }
}
