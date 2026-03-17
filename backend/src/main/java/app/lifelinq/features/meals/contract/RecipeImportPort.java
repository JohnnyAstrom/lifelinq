package app.lifelinq.features.meals.contract;

public interface RecipeImportPort {
    ParsedRecipeImportData importFromUrl(String url);
}
