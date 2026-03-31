package app.lifelinq.features.meals.api;

public record StageRecipeDocumentAssetResponse(
        String assetKind,
        String referenceId,
        String sourceLabel,
        String originalFilename,
        String mimeType
) {
}
