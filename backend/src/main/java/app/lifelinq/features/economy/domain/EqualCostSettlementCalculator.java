package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EqualCostSettlementCalculator implements SettlementCalculator {
    @Override
    public Map<UUID, BigDecimal> calculate(SettlementPeriod period, List<SettlementTransaction> transactions) {
        if (period == null) {
            throw new IllegalArgumentException("period must not be null");
        }
        List<UUID> participants = period.getParticipants().stream()
                .map(PeriodParticipant::getUserId)
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("period participants must not be empty");
        }
        Map<UUID, BigDecimal> balances = new LinkedHashMap<>();
        for (UUID participant : participants) {
            balances.put(participant, SettlementMath.normalize(BigDecimal.ZERO));
        }
        List<SettlementTransaction> activeTransactions = transactions == null
                ? List.of()
                : transactions.stream().filter(t -> t.getDeletedAt() == null).toList();

        for (SettlementTransaction transaction : activeTransactions) {
            BigDecimal amount = SettlementMath.normalize(transaction.getAmount());
            List<BigDecimal> shares = splitEvenly(amount, participants.size());
            for (int i = 0; i < participants.size(); i++) {
                UUID participant = participants.get(i);
                balances.put(participant, SettlementMath.normalize(balances.get(participant).add(shares.get(i))));
            }
            UUID payer = transaction.getPaidByUserId();
            if (!balances.containsKey(payer)) {
                throw new IllegalArgumentException("transaction payer must be participant");
            }
            balances.put(payer, SettlementMath.normalize(balances.get(payer).subtract(amount)));
        }
        return balances;
    }

    private List<BigDecimal> splitEvenly(BigDecimal amount, int participantCount) {
        BigInteger scaledAmount = amount.movePointRight(SettlementMath.SCALE).toBigIntegerExact();
        BigInteger[] division = scaledAmount.divideAndRemainder(BigInteger.valueOf(participantCount));
        BigInteger baseShare = division[0];
        int remainder = division[1].intValueExact();
        List<BigDecimal> shares = new ArrayList<>(participantCount);
        for (int i = 0; i < participantCount; i++) {
            BigInteger units = baseShare;
            if (i < remainder) {
                units = units.add(BigInteger.ONE);
            }
            shares.add(new BigDecimal(units).movePointLeft(SettlementMath.SCALE));
        }
        return shares;
    }
}
