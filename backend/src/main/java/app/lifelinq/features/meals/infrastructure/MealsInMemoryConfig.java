package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.WeekPlanRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!persistence")
public class MealsInMemoryConfig {

    @Bean
    public WeekPlanRepository weekPlanRepository() {
        return new InMemoryWeekPlanRepository();
    }
}
