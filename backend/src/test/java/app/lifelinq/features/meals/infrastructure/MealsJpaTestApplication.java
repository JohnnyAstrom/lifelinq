package app.lifelinq.features.meals.infrastructure;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = WeekPlanJpaRepository.class)
@EnableTransactionManagement
public class MealsJpaTestApplication {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("app.lifelinq.features.meals.infrastructure");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
        factoryBean.setJpaPropertyMap(jpaProperties);
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Bean
    public WeekPlanMapper weekPlanMapper() {
        return new WeekPlanMapper();
    }

    @Bean
    public JpaWeekPlanRepositoryAdapter weekPlanRepository(
            WeekPlanJpaRepository repository,
            WeekPlanMapper mapper
    ) {
        return new JpaWeekPlanRepositoryAdapter(repository, mapper);
    }

    @Bean
    public RecipeMapper recipeMapper() {
        return new RecipeMapper();
    }

    @Bean
    public JpaRecipeRepositoryAdapter recipeRepository(
            RecipeJpaRepository repository,
            RecipeMapper mapper
    ) {
        return new JpaRecipeRepositoryAdapter(repository, mapper);
    }
}
