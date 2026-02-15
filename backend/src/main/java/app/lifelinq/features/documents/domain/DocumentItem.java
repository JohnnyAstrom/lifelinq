package app.lifelinq.features.documents.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DocumentItem {
    private final UUID id;
    private final UUID householdId;
    private final UUID createdByUserId;
    private final String title;
    private final String notes;
    private final LocalDate date;
    private final String category;
    private final List<String> tags;
    private final String externalLink;
    private final Instant createdAt;

    public DocumentItem(
            UUID id,
            UUID householdId,
            UUID createdByUserId,
            String title,
            String notes,
            LocalDate date,
            String category,
            List<String> tags,
            String externalLink,
            Instant createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (createdByUserId == null) {
            throw new IllegalArgumentException("createdByUserId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        this.id = id;
        this.householdId = householdId;
        this.createdByUserId = createdByUserId;
        this.title = title;
        this.notes = notes;
        this.date = date;
        this.category = category;
        this.tags = tags == null ? List.of() : new ArrayList<>(tags);
        this.externalLink = externalLink;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public String getExternalLink() {
        return externalLink;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
