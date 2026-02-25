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
    private final Instant createdAt;
    private final List<Ingredient> ingredients;

    public Recipe(UUID id, UUID groupId, String name, Instant createdAt, List<Ingredient> ingredients) {
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
        this.name = name;
        this.createdAt = createdAt;
        this.ingredients = List.copyOf(normalizedIngredients);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
