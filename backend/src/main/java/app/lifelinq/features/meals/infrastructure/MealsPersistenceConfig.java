package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.WeekPlanRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class MealsPersistenceConfig {

    @Bean
    public WeekPlanMapper weekPlanMapper() {
        return new WeekPlanMapper();
    }

    @Bean
    public WeekPlanRepository weekPlanRepository(
            WeekPlanJpaRepository repository,
            WeekPlanMapper mapper
    ) {
        return new JpaWeekPlanRepositoryAdapter(repository, mapper);
    }
}
