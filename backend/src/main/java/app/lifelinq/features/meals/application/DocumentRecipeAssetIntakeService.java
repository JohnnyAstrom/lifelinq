package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;

public final class DocumentRecipeAssetIntakeService implements RecipeAssetIntakePort {
    private final RecipeDocumentAssetStore documentAssetStore;
    private final RecipeDocumentTextExtractor documentTextExtractor;
    private final DocumentRecipeImportShaper documentImportShaper;

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
        this.documentImportShaper = new DocumentRecipeImportShaper();
    }

    @Override
    public ParsedRecipeImportData extract(RecipeAssetIntakeReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }

        RecipeDocumentAssetPayload document = documentAssetStore.loadDocument(reference.referenceId());
        String extractedText = documentTextExtractor.extract(document);
        return documentImportShaper.shape(reference, extractedText);
    }
}
