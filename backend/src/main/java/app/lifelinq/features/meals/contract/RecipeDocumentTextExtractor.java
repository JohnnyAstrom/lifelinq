package app.lifelinq.features.meals.contract;

public interface RecipeDocumentTextExtractor {
    RecipeDocumentImportAnalysis analyze(RecipeDocumentAssetPayload document);
}
