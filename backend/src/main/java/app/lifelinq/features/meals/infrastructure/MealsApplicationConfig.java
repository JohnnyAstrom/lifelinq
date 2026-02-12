package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.household.contract.EnsureHouseholdMemberUseCase;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MealsApplicationConfig {

    @Bean
    public MealsApplicationService mealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase,
            ShoppingApplicationService shoppingApplicationService,
            Clock clock
    ) {
        return new MealsApplicationService(
                weekPlanRepository,
                ensureHouseholdMemberUseCase,
                shoppingApplicationService,
                clock
        );
    }
}
