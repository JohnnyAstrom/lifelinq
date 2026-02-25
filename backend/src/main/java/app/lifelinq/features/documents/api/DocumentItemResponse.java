package app.lifelinq.features.documents.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class DocumentItemResponse {
    private final UUID id;
    private final UUID groupId;
    private final UUID createdByUserId;
    private final String title;
    private final String notes;
    private final LocalDate date;
    private final String category;
    private final List<String> tags;
    private final String externalLink;
    private final Instant createdAt;

    public DocumentItemResponse(
            UUID id,
            UUID groupId,
            UUID createdByUserId,
            String title,
            String notes,
            LocalDate date,
            String category,
            List<String> tags,
            String externalLink,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.createdByUserId = createdByUserId;
        this.title = title;
        this.notes = notes;
        this.date = date;
        this.category = category;
        this.tags = tags;
        this.externalLink = externalLink;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
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
        return tags;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
