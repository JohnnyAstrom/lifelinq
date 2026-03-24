package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RecipeDraft {
    private final UUID id;
    private final UUID groupId;
    private final String name;
    private final RecipeSource source;
    private final RecipeProvenance provenance;
    private final String servings;
    private final String shortNote;
    private final RecipeInstructions instructions;
    private final RecipeDraftState state;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<Ingredient> ingredients;

    public RecipeDraft(
            UUID id,
            UUID groupId,
            String name,
            RecipeSource source,
            RecipeProvenance provenance,
            String servings,
            String shortNote,
            RecipeInstructions instructions,
            RecipeDraftState state,
            Instant createdAt,
            Instant updatedAt,
            List<Ingredient> ingredients
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (provenance == null) {
            throw new IllegalArgumentException("provenance must not be null");
        }
        if (instructions == null) {
            throw new IllegalArgumentException("instructions must not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
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
        this.name = normalizeOptionalText(name);
        this.source = source;
        this.provenance = provenance;
        this.servings = normalizeOptionalText(servings);
        this.shortNote = normalizeOptionalText(shortNote);
        this.instructions = instructions;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public RecipeSource getSource() {
        return source;
    }

    public RecipeProvenance getProvenance() {
        return provenance;
    }

    public String getServings() {
        return servings;
    }

    public String getShortNote() {
        return shortNote;
    }

    public RecipeInstructions getInstructions() {
        return instructions;
    }

    public RecipeDraftState getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
