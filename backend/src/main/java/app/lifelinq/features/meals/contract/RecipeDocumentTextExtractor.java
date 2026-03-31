package app.lifelinq.features.meals.contract;

public interface RecipeDocumentTextExtractor {
    String extract(RecipeDocumentAssetPayload document);
}
