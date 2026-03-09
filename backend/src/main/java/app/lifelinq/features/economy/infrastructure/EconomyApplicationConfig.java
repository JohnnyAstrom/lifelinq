package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.application.GetActiveSettlementPeriodUseCase;
import app.lifelinq.features.economy.application.InitializeGroupEconomyUseCase;
import app.lifelinq.features.economy.application.CreateSettlementTransactionUseCase;
import app.lifelinq.features.economy.application.CalculateSettlementUseCase;
import app.lifelinq.features.economy.application.CloseSettlementPeriodUseCase;
import app.lifelinq.features.economy.application.EconomyInitializer;
import app.lifelinq.features.economy.application.ListSettlementTransactionsUseCase;
import app.lifelinq.features.economy.application.SoftDeleteSettlementTransactionUseCase;
import app.lifelinq.features.economy.application.UpdateSettlementStrategyUseCase;
import app.lifelinq.features.economy.contract.InitializeGroupEconomyPort;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import app.lifelinq.features.group.contract.GroupFeatureInitializerPort;
import app.lifelinq.features.group.contract.GroupMembershipReadPort;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EconomyApplicationConfig {

    @Bean
    public InitializeGroupEconomyUseCase initializeGroupEconomyUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            GroupMembershipReadPort groupMembershipReadPort,
            Clock clock
    ) {
        return new InitializeGroupEconomyUseCase(
                settlementPeriodRepository,
                groupMembershipReadPort,
                clock
        );
    }

    @Bean
    public InitializeGroupEconomyPort initializeGroupEconomyPort(
            InitializeGroupEconomyUseCase initializeGroupEconomyUseCase
    ) {
        return initializeGroupEconomyUseCase;
    }

    @Bean
    public GroupFeatureInitializerPort economyInitializer(
            InitializeGroupEconomyUseCase initializeGroupEconomyUseCase
    ) {
        return new EconomyInitializer(initializeGroupEconomyUseCase);
    }

    @Bean
    public GetActiveSettlementPeriodUseCase getActiveSettlementPeriodUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            InitializeGroupEconomyUseCase initializeGroupEconomyUseCase
    ) {
        return new GetActiveSettlementPeriodUseCase(
                settlementPeriodRepository,
                initializeGroupEconomyUseCase
        );
    }

    @Bean
    public CreateSettlementTransactionUseCase createSettlementTransactionUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            SettlementTransactionRepository settlementTransactionRepository,
            Clock clock
    ) {
        return new CreateSettlementTransactionUseCase(
                settlementPeriodRepository,
                settlementTransactionRepository,
                clock
        );
    }

    @Bean
    public SoftDeleteSettlementTransactionUseCase softDeleteSettlementTransactionUseCase(
            SettlementTransactionRepository settlementTransactionRepository,
            SettlementPeriodRepository settlementPeriodRepository,
            Clock clock
    ) {
        return new SoftDeleteSettlementTransactionUseCase(
                settlementTransactionRepository,
                settlementPeriodRepository,
                clock
        );
    }

    @Bean
    public ListSettlementTransactionsUseCase listSettlementTransactionsUseCase(
            SettlementTransactionRepository settlementTransactionRepository,
            SettlementPeriodRepository settlementPeriodRepository
    ) {
        return new ListSettlementTransactionsUseCase(
                settlementTransactionRepository,
                settlementPeriodRepository
        );
    }

    @Bean
    public CalculateSettlementUseCase calculateSettlementUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            SettlementTransactionRepository settlementTransactionRepository
    ) {
        return new CalculateSettlementUseCase(
                settlementPeriodRepository,
                settlementTransactionRepository
        );
    }

    @Bean
    public CloseSettlementPeriodUseCase closeSettlementPeriodUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            GroupMembershipReadPort groupMembershipReadPort,
            Clock clock
    ) {
        return new CloseSettlementPeriodUseCase(
                settlementPeriodRepository,
                groupMembershipReadPort,
                clock
        );
    }

    @Bean
    public UpdateSettlementStrategyUseCase updateSettlementStrategyUseCase(
            SettlementPeriodRepository settlementPeriodRepository
    ) {
        return new UpdateSettlementStrategyUseCase(settlementPeriodRepository);
    }
}
