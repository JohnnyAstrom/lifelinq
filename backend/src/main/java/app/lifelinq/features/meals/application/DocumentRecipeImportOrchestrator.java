package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentImportAnalysis;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;

final class DocumentRecipeImportOrchestrator {
    private final RecipeDocumentTextExtractor documentTextExtractor;
    private final DocumentRecipeImportShaper documentImportShaper;

    DocumentRecipeImportOrchestrator(
            RecipeDocumentTextExtractor documentTextExtractor,
            DocumentRecipeImportShaper documentImportShaper
    ) {
        if (documentTextExtractor == null) {
            throw new IllegalArgumentException("documentTextExtractor must not be null");
        }
        if (documentImportShaper == null) {
            throw new IllegalArgumentException("documentImportShaper must not be null");
        }
        this.documentTextExtractor = documentTextExtractor;
        this.documentImportShaper = documentImportShaper;
    }

    ParsedRecipeImportData extract(RecipeAssetIntakeReference reference, RecipeDocumentAssetPayload document) {
        return extract(reference, document, null);
    }

    ParsedRecipeImportData extract(
            RecipeAssetIntakeReference reference,
            RecipeDocumentAssetPayload document,
            ImageRecipeImportOrchestrator imageImportOrchestrator
    ) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }

        RecipeDocumentImportAnalysis analysis = documentTextExtractor.analyze(document);
        return switch (analysis.strategy()) {
            case TEXT_BACKED_DOCUMENT -> documentImportShaper.shape(reference, analysis.extractedText());
            case IMAGE_LIKE_DOCUMENT -> {
                if (imageImportOrchestrator == null) {
                    throw new RecipeAssetIntakeUnavailableException(
                            "That file looks more like a scanned or photo-based recipe. Photo import is not available yet."
                    );
                }
                yield imageImportOrchestrator.extractFromImageLikeDocument(reference, document);
            }
            case TOO_WEAK_TO_CLASSIFY -> throw new RecipeImportFailedException(
                    "We could not get enough readable recipe content from that file to review."
            );
        };
    }
}
