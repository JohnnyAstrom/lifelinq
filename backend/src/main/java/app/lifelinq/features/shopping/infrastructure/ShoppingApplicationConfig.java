package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.meals.application.MealsShoppingPort;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShoppingApplicationConfig {

    @Bean
    public ShoppingApplicationService shoppingApplicationService(
            ShoppingListRepository repository,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            Clock clock
    ) {
        return new ShoppingApplicationService(repository, ensureGroupMemberUseCase, clock);
    }

    @Bean
    public MealsShoppingPort mealsShoppingPort(ShoppingApplicationService shoppingApplicationService) {
        return new MealsShoppingPortAdapter(shoppingApplicationService);
    }
}
