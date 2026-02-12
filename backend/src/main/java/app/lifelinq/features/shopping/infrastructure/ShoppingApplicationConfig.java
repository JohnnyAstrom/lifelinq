package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShoppingApplicationConfig {

    @Bean
    public ShoppingApplicationService shoppingApplicationService(
            ShoppingItemRepository repository,
            EnsureUserExistsUseCase ensureUserExistsUseCase
    ) {
        return new ShoppingApplicationService(repository, ensureUserExistsUseCase);
    }
}
