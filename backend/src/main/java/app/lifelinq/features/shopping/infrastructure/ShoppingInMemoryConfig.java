package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!persistence")
public class ShoppingInMemoryConfig {

    @Bean
    public ShoppingItemRepository shoppingItemRepository() {
        return new InMemoryShoppingItemRepository();
    }
}
