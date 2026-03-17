package app.lifelinq.features.meals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeImportApplicationServiceTest {

    @Test
    void importRecipeDraftNormalizesParsedRecipeIntoReviewableDraft() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                " Apple pie ",
                null,
                "https://example.com/apple-pie",
                " Great dessert ",
                "Mix\r\nBake",
                List.of("2 dl milk", "apple")
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(groupId, userId, "https://example.com/apple-pie");

        assertThat(draft.name()).isEqualTo("Apple pie");
        assertThat(draft.sourceName()).isEqualTo("example.com");
        assertThat(draft.sourceUrl()).isEqualTo("https://example.com/apple-pie");
        assertThat(draft.originKind()).isEqualTo("URL_IMPORT");
        assertThat(draft.shortNote()).isEqualTo("Great dessert");
        assertThat(draft.instructions()).isEqualTo("Mix\nBake");
        assertThat(draft.ingredients()).hasSize(2);
        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.DL);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("milk");
        assertThat(draft.ingredients().get(1).name()).isEqualTo("apple");
    }

    @Test
    void importRecipeDraftRejectsUnsupportedUrlScheme() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData("Recipe", null, url, null, null, List.of("milk"));
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        assertThatThrownBy(() -> service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "ftp://example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("http or https");
    }

    @Test
    void importRecipeDraftFailsWhenImportedRecipeHasNoIngredients() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Recipe",
                "Site",
                url,
                null,
                null,
                List.of()
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        assertThatThrownBy(() -> service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com"))
                .isInstanceOf(RecipeImportFailedException.class)
                .hasMessageContaining("missing ingredients");
    }
}
