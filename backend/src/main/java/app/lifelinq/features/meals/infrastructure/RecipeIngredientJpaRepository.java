package app.lifelinq.features.meals.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeIngredientJpaRepository extends JpaRepository<RecipeIngredientEntity, UUID> {
    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("delete from RecipeIngredientEntity ingredient where ingredient.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") UUID recipeId);
}
