package app.lifelinq.features.meals.contract;

public interface RecipeAssetIntakePort {
    ParsedRecipeImportData extract(RecipeAssetIntakeReference reference);
}
