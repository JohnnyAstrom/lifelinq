package app.lifelinq.features.meals.infrastructure;

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
        name = "recipes",
        indexes = {
                @Index(name = "idx_recipes_group_id", columnList = "group_id")
        }
)
public class RecipeEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "name", nullable = false)
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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "saved_in_recipes", nullable = false)
    private boolean savedInRecipes;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

    protected RecipeEntity() {
    }

    RecipeEntity(
            UUID id,
            UUID groupId,
            String name,
            String sourceName,
            String sourceUrl,
            RecipeOriginKind originKind,
            String servings,
            String shortNote,
            String instructions,
            Instant createdAt,
            Instant updatedAt,
            Instant archivedAt,
            boolean savedInRecipes
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
        this.savedInRecipes = savedInRecipes;
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

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    Instant getArchivedAt() {
        return archivedAt;
    }

    boolean isSavedInRecipes() {
        return savedInRecipes;
    }

    List<RecipeIngredientEntity> getIngredients() {
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
            Instant updatedAt,
            Instant archivedAt,
            boolean savedInRecipes
    ) {
        this.name = name;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.originKind = originKind;
        this.servings = servings;
        this.shortNote = shortNote;
        this.instructions = instructions;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
        this.savedInRecipes = savedInRecipes;
    }

    void replaceIngredients(List<RecipeIngredientEntity> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }
}
