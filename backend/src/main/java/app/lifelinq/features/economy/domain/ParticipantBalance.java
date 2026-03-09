package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record ParticipantBalance(UUID userId, BigDecimal amount) {
}
