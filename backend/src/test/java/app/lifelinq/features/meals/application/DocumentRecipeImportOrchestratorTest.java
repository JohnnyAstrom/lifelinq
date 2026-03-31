package app.lifelinq.features.meals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentImportAnalysis;
import app.lifelinq.features.meals.contract.RecipeDocumentImportStrategy;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DocumentRecipeImportOrchestratorTest {

    @Test
    void routesTextBackedDocumentsIntoTheWorkingDocumentShaper() {
        RecipeDocumentTextExtractor extractor = document -> new RecipeDocumentImportAnalysis(
                RecipeDocumentImportStrategy.TEXT_BACKED_DOCUMENT,
                """
                Creamy Pasta

                Ingredients
                1 pack pasta
                2 dl cream

                Instructions
                Boil pasta.
                Stir in the cream.
                """
        );
        DocumentRecipeImportOrchestrator orchestrator = new DocumentRecipeImportOrchestrator(
                extractor,
                new DocumentRecipeImportShaper()
        );

        var parsed = orchestrator.extract(reference(), payload("recipe.pdf"));

        assertThat(parsed.name()).isEqualTo("Creamy Pasta");
        assertThat(parsed.ingredientLines()).containsExactly("1 pack pasta", "2 dl cream");
        assertThat(parsed.instructions()).contains("Boil pasta.");
    }

    @Test
    void routesImageLikeDocumentsIntoTruthfulPhotoUnavailableState() {
        RecipeDocumentTextExtractor extractor = document -> new RecipeDocumentImportAnalysis(
                RecipeDocumentImportStrategy.IMAGE_LIKE_DOCUMENT,
                null
        );
        DocumentRecipeImportOrchestrator orchestrator = new DocumentRecipeImportOrchestrator(
                extractor,
                new DocumentRecipeImportShaper()
        );

        assertThatThrownBy(() -> orchestrator.extract(reference(), payload("scan.pdf")))
                .isInstanceOf(RecipeAssetIntakeUnavailableException.class)
                .hasMessage("That file looks more like a scanned or photo-based recipe. Photo import is not available yet.");
    }

    @Test
    void failsCalmlyWhenDocumentIsTooWeakToClassifyUsefully() {
        RecipeDocumentTextExtractor extractor = document -> new RecipeDocumentImportAnalysis(
                RecipeDocumentImportStrategy.TOO_WEAK_TO_CLASSIFY,
                "42"
        );
        DocumentRecipeImportOrchestrator orchestrator = new DocumentRecipeImportOrchestrator(
                extractor,
                new DocumentRecipeImportShaper()
        );

        assertThatThrownBy(() -> orchestrator.extract(reference(), payload("weak.pdf")))
                .isInstanceOf(RecipeImportFailedException.class)
                .hasMessage("We could not get enough readable recipe content from that file to review.");
    }

    private RecipeAssetIntakeReference reference() {
        return new RecipeAssetIntakeReference(
                RecipeAssetIntakeKind.DOCUMENT,
                "document-ref-1",
                "recipe.pdf",
                "recipe.pdf",
                "application/pdf"
        );
    }

    private RecipeDocumentAssetPayload payload(String filename) {
        return new RecipeDocumentAssetPayload(
                "document-ref-1",
                filename,
                filename,
                "application/pdf",
                "fake".getBytes(StandardCharsets.UTF_8)
        );
    }
}
