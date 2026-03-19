package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Recipe {
    private final UUID id;
    private final UUID groupId;
    private final String name;
    private final String sourceName;
    private final String sourceUrl;
    private final RecipeOriginKind originKind;
    private final String shortNote;
    private final String instructions;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant archivedAt;
    private final boolean savedInRecipes;
    private final List<Ingredient> ingredients;

    public Recipe(UUID id, UUID groupId, String name, Instant createdAt, List<Ingredient> ingredients) {
        this(id, groupId, name, null, null, RecipeOriginKind.MANUAL, null, null, createdAt, createdAt, null, true, ingredients);
    }

    public Recipe(
            UUID id,
            UUID groupId,
            String name,
            String sourceName,
            String shortNote,
            String instructions,
            Instant createdAt,
            List<Ingredient> ingredients
    ) {
        this(
                id,
                groupId,
                name,
                sourceName,
                null,
                RecipeOriginKind.MANUAL,
                shortNote,
                instructions,
                createdAt,
                createdAt,
                null,
                true,
                ingredients
        );
    }

    public Recipe(
            UUID id,
            UUID groupId,
            String name,
            String sourceName,
            String sourceUrl,
            RecipeOriginKind originKind,
            String shortNote,
            String instructions,
            Instant createdAt,
            Instant updatedAt,
            List<Ingredient> ingredients
    ) {
        this(
                id,
                groupId,
                name,
                sourceName,
                sourceUrl,
                originKind,
                shortNote,
                instructions,
                createdAt,
                updatedAt,
                null,
                true,
                ingredients
        );
    }

    public Recipe(
            UUID id,
            UUID groupId,
            String name,
            String sourceName,
            String sourceUrl,
            RecipeOriginKind originKind,
            String shortNote,
            String instructions,
            Instant createdAt,
            Instant updatedAt,
            Instant archivedAt,
            boolean savedInRecipes,
            List<Ingredient> ingredients
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
        if (archivedAt != null && archivedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("archivedAt must not be before createdAt");
        }
        if (archivedAt != null && archivedAt.isAfter(updatedAt)) {
            throw new IllegalArgumentException("archivedAt must not be after updatedAt");
        }
        if (ingredients == null) {
            throw new IllegalArgumentException("ingredients must not be null");
        }

        Set<Integer> usedPositions = new HashSet<>();
        List<Ingredient> normalizedIngredients = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient == null) {
                throw new IllegalArgumentException("ingredients must not contain null");
            }
            if (!usedPositions.add(ingredient.getPosition())) {
                throw new IllegalArgumentException("ingredient positions must be unique");
            }
            normalizedIngredients.add(ingredient);
        }

        normalizedIngredients.sort(
                Comparator.comparingInt(Ingredient::getPosition).thenComparing(Ingredient::getId)
        );

        this.id = id;
        this.groupId = groupId;
        this.name = name.trim();
        this.sourceName = normalizeOptionalText(sourceName);
        this.sourceUrl = normalizeOptionalText(sourceUrl);
        this.originKind = originKind == null ? RecipeOriginKind.MANUAL : originKind;
        this.shortNote = normalizeOptionalText(shortNote);
        this.instructions = normalizeOptionalText(instructions);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
        this.savedInRecipes = savedInRecipes;
        this.ingredients = List.copyOf(normalizedIngredients);
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public RecipeOriginKind getOriginKind() {
        return originKind;
    }

    public String getShortNote() {
        return shortNote;
    }

    public String getInstructions() {
        return instructions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public boolean isSavedInRecipes() {
        return savedInRecipes;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
