package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;
import app.lifelinq.features.meals.domain.Ingredient;
import java.util.ArrayList;
import java.util.List;

public final class DocumentRecipeAssetIntakeService implements RecipeAssetIntakePort {
    private final RecipeDocumentAssetStore documentAssetStore;
    private final RecipeDocumentTextExtractor documentTextExtractor;

    public DocumentRecipeAssetIntakeService(
            RecipeDocumentAssetStore documentAssetStore,
            RecipeDocumentTextExtractor documentTextExtractor
    ) {
        if (documentAssetStore == null) {
            throw new IllegalArgumentException("documentAssetStore must not be null");
        }
        if (documentTextExtractor == null) {
            throw new IllegalArgumentException("documentTextExtractor must not be null");
        }
        this.documentAssetStore = documentAssetStore;
        this.documentTextExtractor = documentTextExtractor;
    }

    @Override
    public ParsedRecipeImportData extract(RecipeAssetIntakeReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }

        RecipeDocumentAssetPayload document = documentAssetStore.loadDocument(reference.referenceId());
        String extractedText = documentTextExtractor.extract(document);
        RecipeImportDraftSupport.RecipeDraftSeed seed = RecipeImportDraftSupport.importFromText(extractedText);
        return new ParsedRecipeImportData(
                seed.name(),
                seed.source().sourceName(),
                seed.source().sourceUrl(),
                seed.servings(),
                seed.shortNote(),
                seed.instructions().body(),
                toIngredientLines(seed.ingredients())
        );
    }

    private List<String> toIngredientLines(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            String value = ingredient.getRawText() != null ? ingredient.getRawText() : ingredient.getName();
            if (value != null && !value.isBlank()) {
                lines.add(value);
            }
        }
        return List.copyOf(lines);
    }
}
