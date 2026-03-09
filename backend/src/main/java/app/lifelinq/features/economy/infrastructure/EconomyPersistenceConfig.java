package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class EconomyPersistenceConfig {

    @Bean
    public SettlementPeriodMapper settlementPeriodMapper(ObjectMapper objectMapper) {
        return new SettlementPeriodMapper(objectMapper);
    }

    @Bean
    public SettlementPeriodRepository settlementPeriodRepository(
            SettlementPeriodJpaRepository settlementPeriodJpaRepository,
            SettlementPeriodMapper settlementPeriodMapper
    ) {
        return new JpaSettlementPeriodRepositoryAdapter(settlementPeriodJpaRepository, settlementPeriodMapper);
    }

    @Bean
    public SettlementTransactionMapper settlementTransactionMapper() {
        return new SettlementTransactionMapper();
    }

    @Bean
    public SettlementTransactionRepository settlementTransactionRepository(
            SettlementTransactionJpaRepository settlementTransactionJpaRepository,
            SettlementTransactionMapper settlementTransactionMapper
    ) {
        return new JpaSettlementTransactionRepositoryAdapter(
                settlementTransactionJpaRepository,
                settlementTransactionMapper
        );
    }
}
