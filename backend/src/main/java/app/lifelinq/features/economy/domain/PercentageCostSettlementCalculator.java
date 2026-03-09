package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PercentageCostSettlementCalculator implements SettlementCalculator {
    @Override
    public Map<UUID, BigDecimal> calculate(SettlementPeriod period, List<SettlementTransaction> transactions) {
        if (period == null) {
            throw new IllegalArgumentException("period must not be null");
        }
        Map<UUID, BigDecimal> configuredPercentages = period.getStrategySnapshot().getPercentageShares();
        List<UUID> participants = period.getParticipants().stream()
                .map(PeriodParticipant::getUserId)
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        validatePercentages(participants, configuredPercentages);

        Map<UUID, BigDecimal> balances = new LinkedHashMap<>();
        for (UUID participant : participants) {
            balances.put(participant, SettlementMath.normalize(BigDecimal.ZERO));
        }
        List<SettlementTransaction> activeTransactions = transactions == null
                ? List.of()
                : transactions.stream().filter(t -> t.getDeletedAt() == null).toList();

        for (SettlementTransaction transaction : activeTransactions) {
            BigDecimal amount = SettlementMath.normalize(transaction.getAmount());
            Map<UUID, BigDecimal> shares = splitByPercentages(amount, participants, configuredPercentages);
            for (UUID participant : participants) {
                balances.put(
                        participant,
                        SettlementMath.normalize(balances.get(participant).add(shares.get(participant)))
                );
            }
            UUID payer = transaction.getPaidByUserId();
            if (!balances.containsKey(payer)) {
                throw new IllegalArgumentException("transaction payer must be participant");
            }
            balances.put(payer, SettlementMath.normalize(balances.get(payer).subtract(amount)));
        }
        return balances;
    }

    private void validatePercentages(List<UUID> participants, Map<UUID, BigDecimal> percentages) {
        if (percentages == null || percentages.isEmpty()) {
            throw new IllegalArgumentException("percentage shares must not be empty");
        }
        for (UUID participant : participants) {
            if (!percentages.containsKey(participant)) {
                throw new IllegalArgumentException("missing percentage for participant: " + participant);
            }
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal percentage : percentages.values()) {
            if (percentage == null || percentage.signum() < 0) {
                throw new IllegalArgumentException("percentage shares must be >= 0");
            }
            sum = sum.add(percentage);
        }
        if (sum.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("percentage shares must sum to 100");
        }
    }

    private Map<UUID, BigDecimal> splitByPercentages(
            BigDecimal amount,
            List<UUID> participants,
            Map<UUID, BigDecimal> percentages
    ) {
        Map<UUID, BigDecimal> roundedShares = new LinkedHashMap<>();
        List<FractionalShare> fractionalShares = new ArrayList<>();

        BigDecimal roundedSum = BigDecimal.ZERO;
        for (UUID participant : participants) {
            BigDecimal raw = amount
                    .multiply(percentages.get(participant))
                    .divide(new BigDecimal("100"), SettlementMath.SCALE + 8, RoundingMode.HALF_UP);
            BigDecimal roundedDown = raw.setScale(SettlementMath.SCALE, RoundingMode.DOWN);
            roundedShares.put(participant, roundedDown);
            roundedSum = roundedSum.add(roundedDown);
            fractionalShares.add(new FractionalShare(participant, raw.subtract(roundedDown)));
        }

        BigDecimal remainder = SettlementMath.normalize(amount.subtract(roundedSum));
        int unitsToDistribute = remainder.movePointRight(SettlementMath.SCALE).intValueExact();
        fractionalShares.sort(
                Comparator.comparing(FractionalShare::fractionalPart).reversed()
                        .thenComparing(fs -> fs.userId().toString())
        );

        for (int i = 0; i < unitsToDistribute; i++) {
            UUID userId = fractionalShares.get(i % fractionalShares.size()).userId();
            roundedShares.put(userId, SettlementMath.normalize(roundedShares.get(userId).add(SettlementMath.UNIT)));
        }
        return roundedShares;
    }

    private record FractionalShare(UUID userId, BigDecimal fractionalPart) {
    }
}
