package app.lifelinq.features.meals.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeDraftIngredientJpaRepository extends JpaRepository<RecipeDraftIngredientEntity, UUID> {
    @Modifying
    @Query("delete from RecipeDraftIngredientEntity ingredient where ingredient.recipeDraft.id = :draftId")
    void deleteByRecipeDraftId(@Param("draftId") UUID draftId);
}
