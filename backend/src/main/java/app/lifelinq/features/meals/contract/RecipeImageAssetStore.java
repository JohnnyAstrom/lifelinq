package app.lifelinq.features.meals.contract;

public interface RecipeImageAssetStore {
    RecipeAssetIntakeReference stageImage(
            String sourceLabel,
            String originalFilename,
            String mimeType,
            byte[] content
    );

    RecipeImageAssetPayload loadImage(String referenceId);
}
