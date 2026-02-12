package app.lifelinq.features.important.domain;

import java.util.UUID;

public final class ImportantItem {
    private final UUID id;
    private final UUID householdId;
    private final String text;

    public ImportantItem(UUID id, UUID householdId, String text) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        this.id = id;
        this.householdId = householdId;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getText() {
        return text;
    }
}
