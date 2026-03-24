package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.IngredientUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "recipe_draft_ingredients")
public class RecipeDraftIngredientEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_draft_id", nullable = false)
    private RecipeDraftEntity recipeDraft;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "raw_text", length = 2000)
    private String rawText;

    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", length = 16)
    private IngredientUnit unit;

    @Column(name = "position", nullable = false)
    private int position;

    protected RecipeDraftIngredientEntity() {
    }

    RecipeDraftIngredientEntity(
            UUID id,
            RecipeDraftEntity recipeDraft,
            String name,
            String rawText,
            BigDecimal quantity,
            IngredientUnit unit,
            int position
    ) {
        this.id = id;
        this.recipeDraft = recipeDraft;
        this.name = name;
        this.rawText = rawText;
        this.quantity = quantity;
        this.unit = unit;
        this.position = position;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getRawText() {
        return rawText;
    }

    BigDecimal getQuantity() {
        return quantity;
    }

    IngredientUnit getUnit() {
        return unit;
    }

    int getPosition() {
        return position;
    }
}
