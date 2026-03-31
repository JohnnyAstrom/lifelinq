package app.lifelinq.features.meals.application;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import org.junit.jupiter.api.Test;

class DocumentRecipeImportShaperTest {
    private final DocumentRecipeImportShaper shaper = new DocumentRecipeImportShaper();

    @Test
    void prefersStrongContentTitleOverRawFilenameFallback() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.DOCUMENT,
                        "document-ref-1",
                        null,
                        "IMG_4049%20FINAL.pdf",
                        "application/pdf"
                ),
                """
                VEGO TACOS

                Ingredients
                500 g mushrooms
                8 tortillas

                Instructions
                Cook the mushrooms until browned.
                Serve in warm tortillas.
                """
        );

        assertThat(shaped.name()).isEqualTo("Vego Tacos");
        assertThat(shaped.sourceName()).doesNotContain(".pdf");
        assertThat(shaped.sourceName()).doesNotContain("%20");
    }

    @Test
    void separatesIntroIngredientsAndInstructionsMoreCalmly() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.DOCUMENT,
                        "document-ref-2",
                        "weeknight-soup.pdf",
                        "weeknight-soup.pdf",
                        "application/pdf"
                ),
                """
                Warming Lentil Soup
                A calm weeknight soup with lemon and herbs.

                Ingredients
                1 onion
                2 carrots
                1 tbsp olive oil

                Instructions
                Chop the onion and carrots.
                Cook in olive oil until soft.
                Add lentils and simmer gently.
                """
        );

        assertThat(shaped.name()).isEqualTo("Warming Lentil Soup");
        assertThat(shaped.shortNote()).contains("calm weeknight soup");
        assertThat(shaped.ingredientLines()).containsExactly(
                "1 onion",
                "2 carrots",
                "1 tbsp olive oil"
        );
        assertThat(shaped.instructions()).contains("Chop the onion and carrots.");
        assertThat(shaped.instructions()).doesNotContain("A calm weeknight soup");
    }

    @Test
    void ignoresGroupedIngredientHeadingsAndKeepsRealIngredientRows() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.DOCUMENT,
                        "document-ref-3",
                        "recipe.pdf",
                        "recipe.pdf",
                        "application/pdf"
                ),
                """
                VEGO TACOS
                4 portioner

                INGREDIENSER
                PICKLAD LÖK:
                1 rödlök
                1 dl vatten
                ÄRTGUACAMOLE:
                250 g frysta gröna ärter
                1 vitlöksklyfta

                GÖR SÅ HÄR
                Skala och skiva löken tunt.
                Mixa ärterna med vitlök.
                """
        );

        assertThat(shaped.name()).isEqualTo("Vego Tacos");
        assertThat(shaped.servings()).isEqualTo("4");
        assertThat(shaped.ingredientLines()).containsExactly(
                "1 rödlök",
                "1 dl vatten",
                "250 g frysta gröna ärter",
                "1 vitlöksklyfta"
        );
        assertThat(shaped.ingredientLines()).doesNotContain("PICKLAD LÖK:");
        assertThat(shaped.ingredientLines()).doesNotContain("ÄRTGUACAMOLE:");
    }

    @Test
    void keepsVariationContentOutOfCoreInstructionsWhenPossible() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.DOCUMENT,
                        "document-ref-4",
                        "roasted-tomato-pasta.pdf",
                        "roasted-tomato-pasta.pdf",
                        "application/pdf"
                ),
                """
                Roasted Tomato Pasta

                Ingredients
                200 g pasta
                400 g tomatoes

                Instructions
                Roast the tomatoes until soft.
                Mix with cooked pasta.

                Variation
                Add mozzarella before serving.
                """
        );

        assertThat(shaped.instructions()).contains("Roast the tomatoes until soft.");
        assertThat(shaped.instructions()).contains("Mix with cooked pasta.");
        assertThat(shaped.instructions()).doesNotContain("Variation");
        assertThat(shaped.shortNote()).contains("Add mozzarella before serving.");
    }
}
