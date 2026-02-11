package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShoppingApplicationConfig {

    @Bean
    public ShoppingApplicationService shoppingApplicationService(ShoppingItemRepository repository) {
        return new ShoppingApplicationService(repository);
    }
}
