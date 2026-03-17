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
        assertThat(parsed.instructions()).isEqualTo("Mix ingredients\nBake");
        assertThat(parsed.ingredientLines()).containsExactly("2 dl milk", "apple");
    }

    @Test
    void fallsBackToMetadataForMissingStructuredSourceNameAndDescription() {
        String html = """
                <html>
                  <head>
                    <meta property="og:site_name" content="Example Kitchen" />
                    <meta property="og:description" content="A calm imported note" />
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
