package app.lifelinq.features.economy.application;

import app.lifelinq.features.group.contract.GroupFeatureInitializerPort;
import java.util.UUID;

public final class EconomyInitializer implements GroupFeatureInitializerPort {
    private final InitializeGroupEconomyUseCase initializeGroupEconomyUseCase;

    public EconomyInitializer(InitializeGroupEconomyUseCase initializeGroupEconomyUseCase) {
        if (initializeGroupEconomyUseCase == null) {
            throw new IllegalArgumentException("initializeGroupEconomyUseCase must not be null");
        }
        this.initializeGroupEconomyUseCase = initializeGroupEconomyUseCase;
    }

    @Override
    public void initialize(UUID groupId) {
        initializeGroupEconomyUseCase.initializeGroupEconomy(groupId);
    }
}
