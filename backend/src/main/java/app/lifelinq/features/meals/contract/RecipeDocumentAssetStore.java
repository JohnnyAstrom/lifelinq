package app.lifelinq.features.meals.contract;

public interface RecipeDocumentAssetStore {
    RecipeAssetIntakeReference stageDocument(
            String sourceLabel,
            String originalFilename,
            String mimeType,
            byte[] content
    );

    RecipeDocumentAssetPayload loadDocument(String referenceId);
}
