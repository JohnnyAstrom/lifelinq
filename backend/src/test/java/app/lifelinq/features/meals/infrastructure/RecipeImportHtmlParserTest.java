package app.lifelinq.features.meals.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RecipeImportHtmlParserTest {

    private final RecipeImportHtmlParser parser = new RecipeImportHtmlParser(new ObjectMapper());

    @Test
    void parsesStructuredRecipeDataAndPrefersItOverWeakerMetadata() {
        String html = """
                <html>
                  <head>
                    <title>Fallback title</title>
                    <meta property="og:title" content="Fallback og title" />
                    <meta property="og:site_name" content="Fallback Site" />
                    <meta name="description" content="Fallback description" />
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Structured Apple Pie",
                        "description": "Structured note",
                        "author": {"@type":"Person", "name":"Structured Kitchen"},
                        "recipeIngredient": ["2 dl milk", "apple"],
                        "recipeInstructions": [
                          {"@type":"HowToStep", "text":"Mix ingredients"},
                          {"@type":"HowToStep", "text":"Bake"}
                        ]
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/pie", html));

        assertThat(parsed.name()).isEqualTo("Structured Apple Pie");
        assertThat(parsed.sourceName()).isEqualTo("Structured Kitchen");
        assertThat(parsed.shortNote()).isEqualTo("Structured note");
        assertThat(parsed.instructions()).isEqualTo("1. Mix ingredients\n2. Bake");
        assertThat(parsed.ingredientLines()).containsExactly("2 dl milk", "apple");
    }

    @Test
    void fallsBackToMetadataForMissingStructuredSourceNameAndDescription() {
        String html = """
                <html>
                  <head>
                    <meta property="og:site_name" content="Example Kitchen" />
                    <meta property="twitter:description" content="A calm imported note" />
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Toast",
                        "recipeIngredient": ["bread"],
                        "recipeInstructions": "Toast bread"
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/toast", html));

        assertThat(parsed.sourceName()).isEqualTo("Example Kitchen");
        assertThat(parsed.shortNote()).isEqualTo("A calm imported note");
    }

    @Test
    void parsesNestedRecipeNodesAndSplitsStructuredIngredientTextLines() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "mainEntity": {
                          "@type": "Recipe",
                          "name": "Nested soup",
                          "recipeIngredient": [
                            "• 400g mushrooms",
                            "1.5 dl cream\\n2 eggs"
                          ],
                          "recipeInstructions": {
                            "@type": "HowToSection",
                            "itemListElement": [
                              {"@type":"HowToStep", "text":"Cook mushrooms"},
                              {"@type":"HowToStep", "text":"Add cream"}
                            ]
                          }
                        }
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/soup", html));

        assertThat(parsed.name()).isEqualTo("Nested soup");
        assertThat(parsed.ingredientLines()).containsExactly("400g mushrooms", "1.5 dl cream", "2 eggs");
        assertThat(parsed.instructions()).isEqualTo("1. Cook mushrooms\n2. Add cream");
    }

    @Test
    void stripsRepeatedStepPrefixesBeforeNumberingStructuredInstructions() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Toast",
                        "recipeIngredient": ["2 eggs"],
                        "recipeInstructions": [
                          {"@type":"HowToStep", "text":"Step 1: Crack eggs"},
                          {"@type":"HowToStep", "text":"2. Cook gently"}
                        ]
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/toast", html));

        assertThat(parsed.instructions()).isEqualTo("1. Crack eggs\n2. Cook gently");
    }

    @Test
    void splitsInstructionTextBlobOnMultipleLinesIntoNumberedSteps() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Toast",
                        "recipeIngredient": ["bread"],
                        "recipeInstructions": "Heat pan\\nToast bread\\nServe warm"
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/toast", html));

        assertThat(parsed.instructions()).isEqualTo("1. Heat pan\n2. Toast bread\n3. Serve warm");
    }

    @Test
    void splitsInlineNumberedInstructionBlobIntoCleanNumberedSteps() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Toast",
                        "recipeIngredient": ["bread"],
                        "recipeInstructions": "1. Heat pan 2. Toast bread 3. Serve warm"
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/toast", html));

        assertThat(parsed.instructions()).isEqualTo("1. Heat pan\n2. Toast bread\n3. Serve warm");
    }

    @Test
    void preservesAlreadyStructuredHowToSectionsWithoutRegression() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Layered pasta",
                        "recipeIngredient": ["pasta"],
                        "recipeInstructions": [
                          {
                            "@type": "HowToSection",
                            "itemListElement": [
                              {"@type":"HowToStep", "text":"Boil pasta"},
                              {"@type":"HowToStep", "text":"Drain"}
                            ]
                          },
                          {
                            "@type": "HowToSection",
                            "itemListElement": [
                              {"@type":"HowToStep", "text":"Add sauce"},
                              {"@type":"HowToStep", "text":"Serve"}
                            ]
                          }
                        ]
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/pasta", html));

        assertThat(parsed.instructions()).isEqualTo("1. Boil pasta\n2. Drain\n3. Add sauce\n4. Serve");
    }

    @Test
    void parsesNearbyStructuredIngredientAndInstructionVariantsFromItemLists() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Structured stew",
                        "ingredients": {
                          "@type": "ItemList",
                          "itemListElement": [
                            {"@type":"ListItem", "item": {"name":"2 dl stock"}},
                            {"@type":"ListItem", "text":"1 onion"}
                          ]
                        },
                        "instructions": {
                          "@type": "ItemList",
                          "itemListElement": [
                            {"@type":"ListItem", "item": {"text":"Heat stock"}},
                            {"@type":"ListItem", "value":"Add onion"}
                          ]
                        }
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/stew", html));

        assertThat(parsed.ingredientLines()).containsExactly("2 dl stock", "1 onion");
        assertThat(parsed.instructions()).isEqualTo("1. Heat stock\n2. Add onion");
    }

    @Test
    void parsesStructuredFallbackWhenRecipeUsesAlternativeInstructionAndIngredientFields() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context": "https://schema.org",
                        "@type": "Recipe",
                        "name": "Quiet soup",
                        "ingredients": ["2 potatoes", "1 leek"],
                        "instructions": "1. Boil water 2. Add vegetables"
                      }
                    </script>
                  </head>
                </html>
                """;

        var parsed = parser.parse(new FetchedRecipeDocument("https://example.com/quiet-soup", html));

        assertThat(parsed.ingredientLines()).containsExactly("2 potatoes", "1 leek");
        assertThat(parsed.instructions()).isEqualTo("1. Boil water\n2. Add vegetables");
    }

    @Test
    void failsWhenStructuredRecipeDataIsMissing() {
        String html = """
                <html>
                  <head>
                    <meta property="og:title" content="Just a page" />
                  </head>
                  <body>No recipe here</body>
                </html>
                """;

        assertThatThrownBy(() -> parser.parse(new FetchedRecipeDocument("https://example.com/page", html)))
                .isInstanceOf(RecipeImportFailedException.class)
                .hasMessageContaining("structured recipe data");
    }
}
