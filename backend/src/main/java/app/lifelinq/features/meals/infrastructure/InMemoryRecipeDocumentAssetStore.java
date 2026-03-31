package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRecipeDocumentAssetStore implements RecipeDocumentAssetStore {
    private final Map<String, RecipeDocumentAssetPayload> documentsByReferenceId = new ConcurrentHashMap<>();

    @Override
    public RecipeAssetIntakeReference stageDocument(
            String sourceLabel,
            String originalFilename,
            String mimeType,
            byte[] content
    ) {
        String referenceId = "document-" + UUID.randomUUID();
        RecipeDocumentAssetPayload payload = new RecipeDocumentAssetPayload(
                referenceId,
                sourceLabel,
                originalFilename,
                mimeType,
                content
        );
        documentsByReferenceId.put(referenceId, payload);
        return new RecipeAssetIntakeReference(
                RecipeAssetIntakeKind.DOCUMENT,
                referenceId,
                payload.sourceLabel(),
                payload.originalFilename(),
                payload.mimeType()
        );
    }

    @Override
    public RecipeDocumentAssetPayload loadDocument(String referenceId) {
        RecipeDocumentAssetPayload payload = documentsByReferenceId.get(referenceId);
        if (payload == null) {
            throw new RecipeImportFailedException("We could not open that file. Try importing it again.");
        }
        return payload;
    }
}
