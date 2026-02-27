package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.contract.MealsShoppingPort;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MealsApplicationConfig {

    @Bean
    public MealsApplicationService mealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        return new MealsApplicationService(
                weekPlanRepository,
                recipeRepository,
                ensureGroupMemberUseCase,
                mealsShoppingPort,
                clock
        );
    }
}
