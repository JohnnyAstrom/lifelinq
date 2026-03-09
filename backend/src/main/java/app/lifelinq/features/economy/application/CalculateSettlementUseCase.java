package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.EqualCostSettlementCalculator;
import app.lifelinq.features.economy.domain.ParticipantBalance;
import app.lifelinq.features.economy.domain.PercentageCostSettlementCalculator;
import app.lifelinq.features.economy.domain.RecommendedPayment;
import app.lifelinq.features.economy.domain.SettlementCalculator;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementStrategyType;
import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import app.lifelinq.features.group.contract.AccessDeniedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CalculateSettlementUseCase {
    private final SettlementPeriodRepository settlementPeriodRepository;
    private final SettlementTransactionRepository settlementTransactionRepository;
    private final SettlementCalculator equalCostSettlementCalculator;
    private final SettlementCalculator percentageCostSettlementCalculator;

    public CalculateSettlementUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            SettlementTransactionRepository settlementTransactionRepository
    ) {
        this(
                settlementPeriodRepository,
                settlementTransactionRepository,
                new EqualCostSettlementCalculator(),
                new PercentageCostSettlementCalculator()
        );
    }

    CalculateSettlementUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            SettlementTransactionRepository settlementTransactionRepository,
            SettlementCalculator equalCostSettlementCalculator,
            SettlementCalculator percentageCostSettlementCalculator
    ) {
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        if (settlementTransactionRepository == null) {
            throw new IllegalArgumentException("settlementTransactionRepository must not be null");
        }
        if (equalCostSettlementCalculator == null || percentageCostSettlementCalculator == null) {
            throw new IllegalArgumentException("settlement calculators must not be null");
        }
        this.settlementPeriodRepository = settlementPeriodRepository;
        this.settlementTransactionRepository = settlementTransactionRepository;
        this.equalCostSettlementCalculator = equalCostSettlementCalculator;
        this.percentageCostSettlementCalculator = percentageCostSettlementCalculator;
    }

    public CalculateSettlementResult execute(UUID activeGroupId, UUID periodId) {
        if (activeGroupId == null) {
            throw new IllegalArgumentException("activeGroupId must not be null");
        }
        if (periodId == null) {
            throw new IllegalArgumentException("periodId must not be null");
        }
        SettlementPeriod period = settlementPeriodRepository.findById(periodId)
                .orElseThrow(() -> new PeriodNotFoundException(periodId));
        if (!activeGroupId.equals(period.getGroupId())) {
            throw new AccessDeniedException("period does not belong to active group");
        }
        List<SettlementTransaction> activeTransactions = settlementTransactionRepository.findActiveByPeriodId(periodId);

        SettlementCalculator calculator = resolveCalculator(period.getStrategySnapshot().getStrategyType());
        Map<UUID, BigDecimal> calculatedBalances = calculator.calculate(period, activeTransactions);

        List<ParticipantBalance> balances = calculatedBalances.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(UUID::toString)))
                .map(entry -> new ParticipantBalance(entry.getKey(), entry.getValue()))
                .toList();
        List<RecommendedPayment> recommendedPayments = deriveRecommendedPayments(calculatedBalances);
        return new CalculateSettlementResult(balances, recommendedPayments);
    }

    private SettlementCalculator resolveCalculator(SettlementStrategyType strategyType) {
        if (strategyType == SettlementStrategyType.PERCENTAGE_COST) {
            return percentageCostSettlementCalculator;
        }
        return equalCostSettlementCalculator;
    }

    private List<RecommendedPayment> deriveRecommendedPayments(Map<UUID, BigDecimal> balances) {
        List<ParticipantDebt> debtors = new ArrayList<>();
        List<ParticipantDebt> creditors = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            BigDecimal amount = entry.getValue();
            if (amount.signum() > 0) {
                debtors.add(new ParticipantDebt(entry.getKey(), amount));
            } else if (amount.signum() < 0) {
                creditors.add(new ParticipantDebt(entry.getKey(), amount.abs()));
            }
        }

        debtors.sort(Comparator.comparing(d -> d.userId().toString()));
        creditors.sort(Comparator.comparing(c -> c.userId().toString()));

        List<RecommendedPayment> payments = new ArrayList<>();
        int debtorIndex = 0;
        int creditorIndex = 0;
        Map<UUID, BigDecimal> debtorRemaining = toRemainingMap(debtors);
        Map<UUID, BigDecimal> creditorRemaining = toRemainingMap(creditors);

        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            UUID debtorId = debtors.get(debtorIndex).userId();
            UUID creditorId = creditors.get(creditorIndex).userId();
            BigDecimal debtorAmount = debtorRemaining.get(debtorId);
            BigDecimal creditorAmount = creditorRemaining.get(creditorId);

            BigDecimal paymentAmount = debtorAmount.min(creditorAmount);
            if (paymentAmount.signum() > 0) {
                payments.add(new RecommendedPayment(debtorId, creditorId, paymentAmount));
            }

            BigDecimal newDebtorAmount = debtorAmount.subtract(paymentAmount);
            BigDecimal newCreditorAmount = creditorAmount.subtract(paymentAmount);
            debtorRemaining.put(debtorId, newDebtorAmount);
            creditorRemaining.put(creditorId, newCreditorAmount);

            if (newDebtorAmount.signum() == 0) {
                debtorIndex++;
            }
            if (newCreditorAmount.signum() == 0) {
                creditorIndex++;
            }
        }
        return payments;
    }

    private Map<UUID, BigDecimal> toRemainingMap(List<ParticipantDebt> debts) {
        Map<UUID, BigDecimal> remaining = new LinkedHashMap<>();
        for (ParticipantDebt debt : debts) {
            remaining.put(debt.userId(), debt.amount());
        }
        return remaining;
    }

    private record ParticipantDebt(UUID userId, BigDecimal amount) {
    }
}
