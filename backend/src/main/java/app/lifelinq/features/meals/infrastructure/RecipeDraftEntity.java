package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.RecipeDraftState;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "recipe_drafts",
        indexes = {
                @Index(name = "idx_recipe_drafts_group_id", columnList = "group_id")
        }
)
public class RecipeDraftEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "name")
    private String name;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_kind", nullable = false, length = 32)
    private RecipeOriginKind originKind;

    @Column(name = "servings", length = 255)
    private String servings;

    @Column(name = "short_note", length = 1000)
    private String shortNote;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_state", nullable = false, length = 32)
    private RecipeDraftState draftState;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "recipeDraft", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    private List<RecipeDraftIngredientEntity> ingredients = new ArrayList<>();

    protected RecipeDraftEntity() {
    }

    RecipeDraftEntity(
            UUID id,
            UUID groupId,
            String name,
            String sourceName,
            String sourceUrl,
            RecipeOriginKind originKind,
            String servings,
            String shortNote,
            String instructions,
            RecipeDraftState draftState,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.originKind = originKind;
        this.servings = servings;
        this.shortNote = shortNote;
        this.instructions = instructions;
        this.draftState = draftState;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() {
        return id;
    }

    UUID getGroupId() {
        return groupId;
    }

    String getName() {
        return name;
    }

    String getSourceName() {
        return sourceName;
    }

    String getSourceUrl() {
        return sourceUrl;
    }

    RecipeOriginKind getOriginKind() {
        return originKind;
    }

    String getServings() {
        return servings;
    }

    String getShortNote() {
        return shortNote;
    }

    String getInstructions() {
        return instructions;
    }

    RecipeDraftState getDraftState() {
        return draftState;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    List<RecipeDraftIngredientEntity> getIngredients() {
        return ingredients;
    }

    void updateContent(
            String name,
            String sourceName,
            String sourceUrl,
            RecipeOriginKind originKind,
            String servings,
            String shortNote,
            String instructions,
            RecipeDraftState draftState,
            Instant updatedAt
    ) {
        this.name = name;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.originKind = originKind;
        this.servings = servings;
        this.shortNote = shortNote;
        this.instructions = instructions;
        this.draftState = draftState;
        this.updatedAt = updatedAt;
    }

    void replaceIngredients(List<RecipeDraftIngredientEntity> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }
}
