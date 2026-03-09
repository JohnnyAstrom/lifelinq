package app.lifelinq.features.economy.domain;

import java.util.Objects;
import java.util.UUID;

public final class PeriodParticipant {
    private final UUID userId;

    public PeriodParticipant(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PeriodParticipant other)) {
            return false;
        }
        return Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
