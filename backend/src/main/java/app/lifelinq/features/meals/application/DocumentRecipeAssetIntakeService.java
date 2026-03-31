package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;
import app.lifelinq.features.meals.contract.RecipeImageAssetPayload;
import app.lifelinq.features.meals.contract.RecipeImageAssetStore;
import app.lifelinq.features.meals.contract.RecipeImageTextExtractor;

public final class DocumentRecipeAssetIntakeService implements RecipeAssetIntakePort {
    private final RecipeDocumentAssetStore documentAssetStore;
    private final RecipeImageAssetStore imageAssetStore;
    private final DocumentRecipeImportOrchestrator documentImportOrchestrator;
    private final ImageRecipeImportOrchestrator imageImportOrchestrator;

    public DocumentRecipeAssetIntakeService(
            RecipeDocumentAssetStore documentAssetStore,
            RecipeDocumentTextExtractor documentTextExtractor,
            RecipeImageAssetStore imageAssetStore,
            RecipeImageTextExtractor imageTextExtractor
    ) {
        if (documentAssetStore == null) {
            throw new IllegalArgumentException("documentAssetStore must not be null");
        }
        if (documentTextExtractor == null) {
            throw new IllegalArgumentException("documentTextExtractor must not be null");
        }
        if (imageAssetStore == null) {
            throw new IllegalArgumentException("imageAssetStore must not be null");
        }
        if (imageTextExtractor == null) {
            throw new IllegalArgumentException("imageTextExtractor must not be null");
        }
        DocumentRecipeImportShaper documentImportShaper = new DocumentRecipeImportShaper();
        ImageRecipeImportShaper imageImportShaper = new ImageRecipeImportShaper(documentImportShaper);
        this.documentAssetStore = documentAssetStore;
        this.imageAssetStore = imageAssetStore;
        this.documentImportOrchestrator = new DocumentRecipeImportOrchestrator(
                documentTextExtractor,
                documentImportShaper
        );
        this.imageImportOrchestrator = new ImageRecipeImportOrchestrator(
                imageTextExtractor,
                imageImportShaper
        );
    }

    @Override
    public ParsedRecipeImportData extract(RecipeAssetIntakeReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        if (reference.kind() == RecipeAssetIntakeKind.IMAGE) {
            RecipeImageAssetPayload imageAsset = imageAssetStore.loadImage(reference.referenceId());
            return imageImportOrchestrator.extractFromImageAsset(reference, imageAsset);
        }

        RecipeDocumentAssetPayload document = documentAssetStore.loadDocument(reference.referenceId());
        return documentImportOrchestrator.extract(reference, document, imageImportOrchestrator);
    }
}
