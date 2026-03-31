package app.lifelinq.features.meals.contract;

import java.util.Arrays;

public record RecipeDocumentAssetPayload(
        String referenceId,
        String sourceLabel,
        String originalFilename,
        String mimeType,
        byte[] content
) {
    public RecipeDocumentAssetPayload {
        referenceId = normalizeRequired(referenceId, "referenceId");
        sourceLabel = normalizeOptional(sourceLabel);
        originalFilename = normalizeOptional(originalFilename);
        mimeType = normalizeOptional(mimeType);
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("content must not be empty");
        }
        content = Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
