package app.lifelinq.features.meals.api;

public final class CreateRecipeDraftFromAssetRequest {
    private String assetKind;
    private String referenceId;
    private String sourceLabel;
    private String originalFilename;
    private String mimeType;

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
