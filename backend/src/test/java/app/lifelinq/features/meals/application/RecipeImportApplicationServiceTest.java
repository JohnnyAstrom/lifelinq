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
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("2 dl milk");
        assertThat(draft.ingredients().get(1).name()).isEqualTo("apple");
        assertThat(draft.ingredients().get(1).rawText()).isEqualTo("apple");
    }

    @Test
    void importRecipeDraftNormalizesFractionsAttachedUnitsAndBulletIngredients() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "• 400g mushrooms",
                        "1 1/2 dl milk",
                        "- salt to taste",
                        "2 eggs",
                        "1 tbsp olive oil",
                        "12 slices prosciutto"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(6);
        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("400");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.G);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("mushrooms");
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("400g mushrooms");
        assertThat(draft.ingredients().get(1).quantity()).isEqualByComparingTo("1.5");
        assertThat(draft.ingredients().get(1).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.DL);
        assertThat(draft.ingredients().get(1).name()).isEqualTo("milk");
        assertThat(draft.ingredients().get(1).rawText()).isEqualTo("1 1/2 dl milk");
        assertThat(draft.ingredients().get(2).quantity()).isNull();
        assertThat(draft.ingredients().get(2).name()).isEqualTo("salt to taste");
        assertThat(draft.ingredients().get(2).rawText()).isEqualTo("salt to taste");
        assertThat(draft.ingredients().get(3).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(3).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.PCS);
        assertThat(draft.ingredients().get(3).name()).isEqualTo("eggs");
        assertThat(draft.ingredients().get(3).rawText()).isEqualTo("2 eggs");
        assertThat(draft.ingredients().get(4).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(4).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(4).name()).isEqualTo("olive oil");
        assertThat(draft.ingredients().get(4).rawText()).isEqualTo("1 tbsp olive oil");
        assertThat(draft.ingredients().get(5).quantity()).isNull();
        assertThat(draft.ingredients().get(5).unit()).isNull();
        assertThat(draft.ingredients().get(5).name()).isEqualTo("12 slices prosciutto");
        assertThat(draft.ingredients().get(5).rawText()).isEqualTo("12 slices prosciutto");
    }

    @Test
    void importRecipeDraftShapesCommonSwedishMeasureTokensIntoCleanerFallbackNames() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "msk tomatpuré",
                        "msk koncentrerad kycklingfond",
                        "tsk dijonsenap",
                        "krm salt",
                        "½ msk english mustard"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(5);
        assertThat(draft.ingredients().get(0).quantity()).isNull();
        assertThat(draft.ingredients().get(0).unit()).isNull();
        assertThat(draft.ingredients().get(0).name()).isEqualTo("tomatpuré");
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("msk tomatpuré");
        assertThat(draft.ingredients().get(1).name()).isEqualTo("koncentrerad kycklingfond");
        assertThat(draft.ingredients().get(1).rawText()).isEqualTo("msk koncentrerad kycklingfond");
        assertThat(draft.ingredients().get(2).name()).isEqualTo("dijonsenap");
        assertThat(draft.ingredients().get(2).rawText()).isEqualTo("tsk dijonsenap");
        assertThat(draft.ingredients().get(3).name()).isEqualTo("salt");
        assertThat(draft.ingredients().get(3).rawText()).isEqualTo("krm salt");
        assertThat(draft.ingredients().get(4).quantity()).isEqualByComparingTo("0.5");
        assertThat(draft.ingredients().get(4).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(4).name()).isEqualTo("english mustard");
        assertThat(draft.ingredients().get(4).rawText()).isEqualTo("½ msk english mustard");
    }

    @Test
    void importRecipeDraftParsesCommonSwedishKitchenUnitsIntoStructuredIngredients() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2 msk tomatpuré",
                        "3 tsk senap",
                        "1 krm salt"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(3);
        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("tomatpuré");
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("2 msk tomatpuré");
        assertThat(draft.ingredients().get(1).quantity()).isEqualByComparingTo("3");
        assertThat(draft.ingredients().get(1).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TSP);
        assertThat(draft.ingredients().get(1).name()).isEqualTo("senap");
        assertThat(draft.ingredients().get(2).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(2).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.KRM);
        assertThat(draft.ingredients().get(2).name()).isEqualTo("salt");
    }

    @Test
    void importRecipeDraftParsesCommonEnglishKitchenUnitsIntoStructuredIngredients() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2 tbsp tomato puree",
                        "3 tsp mustard",
                        "½ tbsp english mustard"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(3);
        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("tomato puree");
        assertThat(draft.ingredients().get(1).quantity()).isEqualByComparingTo("3");
        assertThat(draft.ingredients().get(1).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TSP);
        assertThat(draft.ingredients().get(1).name()).isEqualTo("mustard");
        assertThat(draft.ingredients().get(2).quantity()).isEqualByComparingTo("0.5");
        assertThat(draft.ingredients().get(2).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(2).name()).isEqualTo("english mustard");
    }

    @Test
    void importRecipeDraftRecognizesAdditionalKitchenUnitAliasesAcrossSwedishAndEnglish() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2 matsk tomatpuré",
                        "1 tesk senap",
                        "1 kryddmått salt",
                        "2 tbs olive oil",
                        "1 teasp cumin"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(5);
        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("tomatpuré");
        assertThat(draft.ingredients().get(1).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(1).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TSP);
        assertThat(draft.ingredients().get(1).name()).isEqualTo("senap");
        assertThat(draft.ingredients().get(2).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(2).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.KRM);
        assertThat(draft.ingredients().get(2).name()).isEqualTo("salt");
        assertThat(draft.ingredients().get(3).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(3).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(3).name()).isEqualTo("olive oil");
        assertThat(draft.ingredients().get(4).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(4).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TSP);
        assertThat(draft.ingredients().get(4).name()).isEqualTo("cumin");
    }

    @Test
    void importRecipeDraftRecognizesAttachedAndUppercaseKitchenUnitAliases() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2MSK tomatpuré",
                        "1TSK senap",
                        "1KRYDDMÅTT salt",
                        "2Tblsp olive oil"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(4);
        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("tomatpuré");
        assertThat(draft.ingredients().get(1).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(1).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TSP);
        assertThat(draft.ingredients().get(1).name()).isEqualTo("senap");
        assertThat(draft.ingredients().get(2).quantity()).isEqualByComparingTo("1");
        assertThat(draft.ingredients().get(2).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.KRM);
        assertThat(draft.ingredients().get(2).name()).isEqualTo("salt");
        assertThat(draft.ingredients().get(3).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(3).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
        assertThat(draft.ingredients().get(3).name()).isEqualTo("olive oil");
    }

    @Test
    void importRecipeDraftFiltersObviousNonIngredientLabelsAndHeadings() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "Ingredients",
                        "Till servering",
                        "Sauce:",
                        "2 eggs",
                        "1 tbsp olive oil"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(2);
        assertThat(draft.ingredients().get(0).name()).isEqualTo("eggs");
        assertThat(draft.ingredients().get(0).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.PCS);
        assertThat(draft.ingredients().get(1).name()).isEqualTo("olive oil");
        assertThat(draft.ingredients().get(1).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.TBSP);
    }

    @Test
    void importRecipeDraftFallsBackConservativelyForUnsupportedMeasureTokensInsteadOfFalsePcs() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2 skivor bacon",
                        "1 klyfta vitlök",
                        "2 burkar tomater"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(3);
        assertThat(draft.ingredients().get(0).quantity()).isNull();
        assertThat(draft.ingredients().get(0).unit()).isNull();
        assertThat(draft.ingredients().get(0).name()).isEqualTo("2 skivor bacon");
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("2 skivor bacon");
        assertThat(draft.ingredients().get(1).quantity()).isNull();
        assertThat(draft.ingredients().get(1).unit()).isNull();
        assertThat(draft.ingredients().get(1).name()).isEqualTo("1 klyfta vitlök");
        assertThat(draft.ingredients().get(1).rawText()).isEqualTo("1 klyfta vitlök");
        assertThat(draft.ingredients().get(2).quantity()).isNull();
        assertThat(draft.ingredients().get(2).unit()).isNull();
        assertThat(draft.ingredients().get(2).name()).isEqualTo("2 burkar tomater");
        assertThat(draft.ingredients().get(2).rawText()).isEqualTo("2 burkar tomater");
    }

    @Test
    void importRecipeDraftPreservesMeaningForUnsupportedSwedishClovePhrases() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2 klyftor vitlök",
                        "1 klyfta vitlök"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(2);
        assertThat(draft.ingredients().get(0).quantity()).isNull();
        assertThat(draft.ingredients().get(0).unit()).isNull();
        assertThat(draft.ingredients().get(0).name()).isEqualTo("2 klyftor vitlök");
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("2 klyftor vitlök");
        assertThat(draft.ingredients().get(1).quantity()).isNull();
        assertThat(draft.ingredients().get(1).unit()).isNull();
        assertThat(draft.ingredients().get(1).name()).isEqualTo("1 klyfta vitlök");
        assertThat(draft.ingredients().get(1).rawText()).isEqualTo("1 klyfta vitlök");
    }

    @Test
    void importRecipeDraftPreservesMeaningForUnsupportedEnglishClovePhrases() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of("2 cloves garlic")
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(1);
        assertThat(draft.ingredients().get(0).quantity()).isNull();
        assertThat(draft.ingredients().get(0).unit()).isNull();
        assertThat(draft.ingredients().get(0).name()).isEqualTo("2 cloves garlic");
        assertThat(draft.ingredients().get(0).rawText()).isEqualTo("2 cloves garlic");
    }

    @Test
    void importRecipeDraftPrefersRawFallbackForUnclearMeasuredPhrases() {
        EnsureGroupMemberUseCase membership = (g, u) -> {};
        RecipeImportPort port = url -> new ParsedRecipeImportData(
                "Soup",
                "Recipe Site",
                url,
                null,
                "Cook",
                List.of(
                        "2 heaped tbsp sugar",
                        "1 generous dl cream",
                        "2 large eggs"
                )
        );
        RecipeImportApplicationService service = new RecipeImportApplicationService(membership, port);

        var draft = service.importRecipeDraft(UUID.randomUUID(), UUID.randomUUID(), "https://example.com/soup");

        assertThat(draft.ingredients()).hasSize(3);
        assertThat(draft.ingredients().get(0).quantity()).isNull();
        assertThat(draft.ingredients().get(0).unit()).isNull();
        assertThat(draft.ingredients().get(0).name()).isEqualTo("2 heaped tbsp sugar");
        assertThat(draft.ingredients().get(1).quantity()).isNull();
        assertThat(draft.ingredients().get(1).unit()).isNull();
        assertThat(draft.ingredients().get(1).name()).isEqualTo("1 generous dl cream");
        assertThat(draft.ingredients().get(2).quantity()).isEqualByComparingTo("2");
        assertThat(draft.ingredients().get(2).unit()).isEqualTo(app.lifelinq.features.meals.contract.IngredientUnitView.PCS);
        assertThat(draft.ingredients().get(2).name()).isEqualTo("large eggs");
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
