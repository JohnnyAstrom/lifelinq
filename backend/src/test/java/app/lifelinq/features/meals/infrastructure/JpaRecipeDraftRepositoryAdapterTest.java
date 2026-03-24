package app.lifelinq.features.meals.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.RecipeDraft;
import app.lifelinq.features.meals.domain.RecipeDraftState;
import app.lifelinq.features.meals.domain.RecipeInstructions;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import app.lifelinq.features.meals.domain.RecipeProvenance;
import app.lifelinq.features.meals.domain.RecipeSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MealsJpaTestApplication.class)
@ActiveProfiles("test")
class JpaRecipeDraftRepositoryAdapterTest {

    @Autowired
    private JpaRecipeDraftRepositoryAdapter repository;

    @Test
    void savesAndLoadsDraftWithStructuredSourceProvenanceAndIngredients() {
        UUID groupId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        RecipeDraft draft = new RecipeDraft(
                draftId,
                groupId,
                "Imported Soup",
                new RecipeSource("Example Kitchen", "https://example.com/soup"),
                new RecipeProvenance(RecipeOriginKind.URL_IMPORT, "https://example.com/soup"),
                "4 servings",
                "Comfort food",
                new RecipeInstructions("Simmer gently"),
                RecipeDraftState.DRAFT_NEEDS_REVIEW,
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:05:00Z"),
                List.of(
                        new Ingredient(
                                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                                "Milk",
                                "1 dl milk",
                                BigDecimal.ONE,
                                IngredientUnit.DL,
                                2
                        ),
                        new Ingredient(
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "Onion",
                                null,
                                null,
                                null,
                                1
                        )
                )
        );

        repository.save(draft);
        Optional<RecipeDraft> loaded = repository.findByIdAndGroupId(draftId, groupId);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getState()).isEqualTo(RecipeDraftState.DRAFT_NEEDS_REVIEW);
        assertThat(loaded.get().getSource().sourceName()).isEqualTo("Example Kitchen");
        assertThat(loaded.get().getProvenance().originKind()).isEqualTo(RecipeOriginKind.URL_IMPORT);
        assertThat(loaded.get().getIngredients())
                .extracting(Ingredient::getName)
                .containsExactly("Onion", "Milk");
    }

    @Test
    void deleteRemovesDraftByScopedIdentity() {
        UUID groupId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        RecipeDraft draft = new RecipeDraft(
                draftId,
                groupId,
                "Manual",
                new RecipeSource(null, null),
                new RecipeProvenance(RecipeOriginKind.MANUAL, null),
                null,
                null,
                new RecipeInstructions(null),
                RecipeDraftState.DRAFT_OPEN,
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:00:00Z"),
                List.of()
        );

        repository.save(draft);
        repository.delete(draft);

        assertThat(repository.findByIdAndGroupId(draftId, groupId)).isEmpty();
    }
}
