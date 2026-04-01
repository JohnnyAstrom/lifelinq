package app.lifelinq.features.meals.application;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        assertThat(shaped.ingredientLines()).doesNotContain("4 portioner");
        assertThat(shaped.ingredientLines()).doesNotContain("I samarbete med Fontanafredda");
    }

    @Test
    void filtersCombinedMetadataShortJunkAndCaptionLikeRowsFromIngredients() {
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.IMAGE,
                        "image-ref-4",
                        "recipe-photo.jpg",
                        "recipe-photo.jpg",
                        "image/jpeg"
                ),
                """
                Italiensk kycklinggryta

                Ingredienser
                wu
                18 ingredienser 45 min
                Fontanafredda Piemonte Barbera Nebbiolo
                700 g kycklinglårfilé
                2 morötter

                Gör så här
                Skär kycklingen i bitar.
                """
        );

        assertThat(shaped.ingredientLines()).containsExactly(
                "700 g kycklinglårfilé",
                "2 morötter"
        );
        assertThat(shaped.ingredientLines()).doesNotContain("wu");
        assertThat(shaped.ingredientLines()).doesNotContain("18 ingredienser 45 min");
        assertThat(shaped.ingredientLines()).doesNotContain("Fontanafredda Piemonte Barbera Nebbiolo");
        assertThat(shaped.instructions()).contains("Skär kycklingen i bitar.");
    }

    @Test
    void keepsTopRecipeTitleUsableForDocumentShaping() {
        String extractedText = """
                Italiensk kycklinggryta
                med rödvin och
                rosmarin

                Ingredienser
                700 g kycklinglårfilé

                Gör så här
                Skär kycklingen i bitar.
                """;
        String handoffText = cleanedText(extractedText);
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.IMAGE,
                        "image-ref-2",
                        "recipe-photo.jpg",
                        "recipe-photo.jpg",
                        "image/jpeg"
                ),
                extractedText
        );

        assertThat(handoffText).startsWith("Italiensk kycklinggryta med rödvin och rosmarin");
        assertThat(shaped.name()).isEqualTo("Italiensk kycklinggryta med rödvin och rosmarin");
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

    @Test
    void preservesBoundedSectionMeaningBeforeDocumentParsing() {
        String extractedText = """
                Italiensk kycklinggryta
                med rödvin och
                rosmarin
                18 ingredienser 45 min
                En mustig rödvinsgryta med kyckling och tomat.

                Gör så här
                Skär kycklingen i bitar.
                Tillsätt tomater och låt sjuda.

                Ingredienser
                700 g kycklinglårfilé
                2 morötter
                """;
        String handoffText = cleanedText(extractedText);
        ParsedRecipeImportData shaped = shaper.shape(
                new RecipeAssetIntakeReference(
                        RecipeAssetIntakeKind.IMAGE,
                        "image-ref-5",
                        "recipe-photo.jpg",
                        "recipe-photo.jpg",
                        "image/jpeg"
                ),
                extractedText
        );

        assertThat(handoffText).startsWith("Italiensk kycklinggryta med rödvin och rosmarin");
        assertThat(handoffText).contains("700 g kycklinglårfilé");
        assertThat(handoffText).contains("2 morötter");
        assertThat(handoffText).contains("Gör så här");
        assertThat(handoffText).doesNotContain("18 ingredienser 45 min");
        assertThat(shaped.name()).isEqualTo("Italiensk kycklinggryta med rödvin och rosmarin");
        assertThat(shaped.shortNote()).contains("En mustig rödvinsgryta");
        assertThat(shaped.shortNote()).doesNotContain("18 ingredienser 45 min");
        assertThat(shaped.ingredientLines()).containsExactly(
                "700 g kycklinglårfilé",
                "2 morötter"
        );
        assertThat(shaped.ingredientLines()).doesNotContain("med rödvin och");
        assertThat(shaped.instructions()).contains("Skär kycklingen i bitar.");
        assertThat(shaped.instructions()).contains("Tillsätt tomater och låt sjuda.");
        assertThat(shaped.instructions()).doesNotContain("En mustig rödvinsgryta");
    }

    private String cleanedText(String extractedText) {
        try {
            Method method = ImageRecipeImportShaper.class.getDeclaredMethod("cleanOcrText", String.class);
            method.setAccessible(true);
            return (String) method.invoke(shaper, extractedText);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionError(ex.getTargetException());
        }
    }
}
