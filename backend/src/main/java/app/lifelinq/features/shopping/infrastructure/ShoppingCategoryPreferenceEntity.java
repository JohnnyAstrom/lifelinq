package app.lifelinq.features.shopping.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "shopping_category_preferences",
        indexes = {
                @Index(name = "idx_shopping_category_preferences_group_id", columnList = "group_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shopping_category_preferences_group_title",
                        columnNames = {"group_id", "normalized_title"}
                )
        }
)
public class ShoppingCategoryPreferenceEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle;

    @Column(name = "preferred_category", nullable = false)
    private String preferredCategory;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ShoppingCategoryPreferenceEntity() {
    }

    ShoppingCategoryPreferenceEntity(
            UUID id,
            UUID groupId,
            String normalizedTitle,
            String preferredCategory,
            Instant updatedAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.normalizedTitle = normalizedTitle;
        this.preferredCategory = preferredCategory;
        this.updatedAt = updatedAt;
    }

    UUID getId() {
        return id;
    }

    UUID getGroupId() {
        return groupId;
    }

    String getNormalizedTitle() {
        return normalizedTitle;
    }

    String getPreferredCategory() {
        return preferredCategory;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void updatePreferredCategory(String nextCategory, Instant nextUpdatedAt) {
        this.preferredCategory = nextCategory;
        this.updatedAt = nextUpdatedAt;
    }
}
