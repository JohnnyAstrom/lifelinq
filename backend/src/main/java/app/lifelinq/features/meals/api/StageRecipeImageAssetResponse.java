package app.lifelinq.features.meals.api;

public final class StageRecipeImageAssetResponse {
    private final String assetKind;
    private final String referenceId;
    private final String sourceLabel;
    private final String originalFilename;
    private final String mimeType;

    public StageRecipeImageAssetResponse(
            String assetKind,
            String referenceId,
            String sourceLabel,
            String originalFilename,
            String mimeType
    ) {
        this.assetKind = assetKind;
        this.referenceId = referenceId;
        this.sourceLabel = sourceLabel;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
    }

    public String getAssetKind() {
        return assetKind;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }
}
