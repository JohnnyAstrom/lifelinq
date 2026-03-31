package app.lifelinq.features.meals.application;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import org.junit.jupiter.api.Test;

class ImageRecipeImportShaperTest {
    private final ImageRecipeImportShaper shaper = new ImageRecipeImportShaper(new DocumentRecipeImportShaper());

    @Test
    void filtersMetadataAndPromotionalNoiseFromIngredientSection() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.IMAGE,
                        "image-ref-1",
                        "recipe-photo.jpg",
                        "recipe-photo.jpg",
                        "image/jpeg"
                ),
                """
                Italiensk kycklinggryta

                Ingredienser
                18 ingredienser
                45 min
                4 portioner
                700 g kycklinglårfilé
                I samarbete med Fontanafredda
                2 morötter

                Gör så här
                Skär kycklingen i bitar.
                Bryn den gyllene.
                """
        );

        assertThat(shaped.ingredientLines()).contains("700 g kycklinglårfilé", "2 morötter");
        assertThat(shaped.ingredientLines()).doesNotContain("18 ingredienser");
        assertThat(shaped.ingredientLines()).doesNotContain("45 min");
        assertThat(shaped.ingredientLines()).doesNotContain("I samarbete med Fontanafredda");
    }

    @Test
    void keepsTopRecipeTitleUsableForDocumentShaping() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.IMAGE,
                        "image-ref-2",
                        "recipe-photo.jpg",
                        "recipe-photo.jpg",
                        "image/jpeg"
                ),
                """
                Italiensk kycklinggryta
                med rödvin och
                rosmarin

                Ingredienser
                700 g kycklinglårfilé

                Gör så här
                Skär kycklingen i bitar.
                """
        );

        assertThat(shaped.name()).isEqualTo("Italiensk kycklinggryta");
    }

    @Test
    void keepsMethodTextAvailableForInstructions() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.IMAGE,
                        "image-ref-3",
                        "recipe-photo.jpg",
                        "recipe-photo.jpg",
                        "image/jpeg"
                ),
                """
                Kycklinggryta

                Ingredienser
                700 g kycklinglårfilé

                Gör så här
                Skär kycklingen i bitar.
                Tillsätt tomater och låt sjuda.
                """
        );

        assertThat(shaped.instructions()).contains("Skär kycklingen i bitar.");
        assertThat(shaped.instructions()).contains("Tillsätt tomater och låt sjuda.");
    }
}
