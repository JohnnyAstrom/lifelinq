package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;

public final class DocumentRecipeAssetIntakeService implements RecipeAssetIntakePort {
    private final RecipeDocumentAssetStore documentAssetStore;
    private final DocumentRecipeImportOrchestrator documentImportOrchestrator;

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
        DocumentRecipeImportShaper documentImportShaper = new DocumentRecipeImportShaper();
        this.documentAssetStore = documentAssetStore;
        this.documentImportOrchestrator = new DocumentRecipeImportOrchestrator(
                documentTextExtractor,
                documentImportShaper
        );
    }

    @Override
    public ParsedRecipeImportData extract(RecipeAssetIntakeReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }

        RecipeDocumentAssetPayload document = documentAssetStore.loadDocument(reference.referenceId());
        return documentImportOrchestrator.extract(reference, document);
    }
}
