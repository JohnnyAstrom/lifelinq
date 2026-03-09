package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!persistence")
public class EconomyInMemoryConfig {

    @Bean
    public SettlementPeriodRepository settlementPeriodRepository() {
        return new InMemorySettlementPeriodRepository();
    }

    @Bean
    public SettlementTransactionRepository settlementTransactionRepository() {
        return new InMemorySettlementTransactionRepository();
    }
}
