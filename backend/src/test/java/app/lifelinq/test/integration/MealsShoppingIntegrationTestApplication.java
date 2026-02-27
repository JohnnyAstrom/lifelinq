package app.lifelinq.test.integration;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.group.infrastructure.GroupJpaRepository;
import app.lifelinq.features.group.infrastructure.GroupPersistenceConfig;
import app.lifelinq.features.meals.infrastructure.MealsApplicationConfig;
import app.lifelinq.features.meals.infrastructure.MealsPersistenceConfig;
import app.lifelinq.features.meals.infrastructure.WeekPlanJpaRepository;
import app.lifelinq.features.shopping.infrastructure.ShoppingApplicationConfig;
import app.lifelinq.features.shopping.infrastructure.ShoppingListJpaRepository;
import app.lifelinq.features.shopping.infrastructure.ShoppingPersistenceConfig;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = {
        GroupJpaRepository.class,
        ShoppingListJpaRepository.class,
        WeekPlanJpaRepository.class
})
@EnableTransactionManagement
@Import({
        GroupPersistenceConfig.class,
        ShoppingPersistenceConfig.class,
        MealsPersistenceConfig.class,
        ShoppingApplicationConfig.class,
        MealsApplicationConfig.class
})
public class MealsShoppingIntegrationTestApplication {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan(
                "app.lifelinq.features.group.infrastructure",
                "app.lifelinq.features.shopping.infrastructure",
                "app.lifelinq.features.meals.infrastructure"
        );
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
    public EnsureGroupMemberUseCase ensureGroupMemberUseCase(
            app.lifelinq.features.group.domain.MembershipRepository membershipRepository
    ) {
        return (groupId, actorUserId) -> {
            var memberships = membershipRepository.findByGroupId(groupId);
            for (var membership : memberships) {
                if (membership.getUserId().equals(actorUserId)) {
                    return;
                }
            }
            throw new AccessDeniedException("Actor is not a member of the group");
        };
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
