package app.lifelinq.features.meals.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
                @Index(name = "idx_recipes_household_id", columnList = "household_id")
        }
)
public class RecipeEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID groupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

    protected RecipeEntity() {
    }

    RecipeEntity(UUID id, UUID groupId, String name, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.createdAt = createdAt;
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

    Instant getCreatedAt() {
        return createdAt;
    }

    List<RecipeIngredientEntity> getIngredients() {
        return ingredients;
    }
}
