package app.lifelinq.features.documents.infrastructure;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String title;

    private String notes;

    private LocalDate date;

    private String category;

    @ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private String externalLink;

    @Column(nullable = false)
    private Instant createdAt;

    protected DocumentEntity() {
    }

    public DocumentEntity(
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
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
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
