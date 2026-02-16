package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "recipe_ingredients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_recipe_ingredients_recipe_position", columnNames = {"recipe_id", "position"})
        },
        indexes = {
                @Index(name = "idx_recipe_ingredients_recipe_id", columnList = "recipe_id")
        }
)
public class RecipeIngredientEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private RecipeEntity recipe;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "quantity", precision = 12, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private ShoppingUnit unit;

    @Column(name = "position", nullable = false)
    private int position;

    protected RecipeIngredientEntity() {
    }

    RecipeIngredientEntity(
            UUID id,
            RecipeEntity recipe,
            String name,
            BigDecimal quantity,
            ShoppingUnit unit,
            int position
    ) {
        this.id = id;
        this.recipe = recipe;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.position = position;
    }

    UUID getId() {
        return id;
    }

    RecipeEntity getRecipe() {
        return recipe;
    }

    String getName() {
        return name;
    }

    BigDecimal getQuantity() {
        return quantity;
    }

    ShoppingUnit getUnit() {
        return unit;
    }

    int getPosition() {
        return position;
    }
}
