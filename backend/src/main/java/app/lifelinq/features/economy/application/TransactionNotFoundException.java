package app.lifelinq.features.economy.application;

import java.util.UUID;

public final class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID transactionId) {
        super("transaction not found: " + transactionId);
    }
}
