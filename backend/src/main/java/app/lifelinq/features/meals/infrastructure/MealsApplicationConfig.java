package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.application.DocumentRecipeAssetIntakeService;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.application.RecipeImportApplicationService;
import app.lifelinq.features.meals.contract.MealsShoppingPort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;
import app.lifelinq.features.meals.contract.RecipeImageAssetStore;
import app.lifelinq.features.meals.contract.RecipeImageTextExtractor;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalRepository;
import app.lifelinq.features.meals.domain.MealMemoryRepository;
import app.lifelinq.features.meals.domain.RecipeDraftRepository;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MealsApplicationConfig {

    @Bean
    public MealsApplicationService mealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            RecipeDraftRepository recipeDraftRepository,
            MealMemoryRepository mealMemoryRepository,
            HouseholdPreferenceSignalRepository householdPreferenceSignalRepository,
            RecipeImportPort recipeImportPort,
            ObjectProvider<RecipeAssetIntakePort> recipeAssetIntakePortProvider,
            ObjectProvider<RecipeDocumentAssetStore> recipeDocumentAssetStoreProvider,
            ObjectProvider<RecipeImageAssetStore> recipeImageAssetStoreProvider,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        return new MealsApplicationService(
                weekPlanRepository,
                recipeRepository,
                recipeDraftRepository,
                mealMemoryRepository,
                householdPreferenceSignalRepository,
                recipeImportPort,
                recipeAssetIntakePortProvider.getIfAvailable(),
                recipeDocumentAssetStoreProvider.getIfAvailable(),
                recipeImageAssetStoreProvider.getIfAvailable(),
                ensureGroupMemberUseCase,
                mealsShoppingPort,
                clock
        );
    }

    @Bean
    public RecipeImportApplicationService recipeImportApplicationService(
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            RecipeImportPort recipeImportPort
    ) {
        return new RecipeImportApplicationService(
                ensureGroupMemberUseCase,
                recipeImportPort
        );
    }

    @Bean
    public RecipeDocumentAssetStore recipeDocumentAssetStore() {
        return new InMemoryRecipeDocumentAssetStore();
    }

    @Bean
    public RecipeDocumentTextExtractor recipeDocumentTextExtractor() {
        return new PdfBoxRecipeDocumentTextExtractor();
    }

    @Bean
    public RecipeImageAssetStore recipeImageAssetStore() {
        return new InMemoryRecipeImageAssetStore();
    }

    @Bean
    public RecipeImageTextExtractor recipeImageTextExtractor() {
        return new Tess4jRecipeImageTextExtractor();
    }

    @Bean
    public RecipeAssetIntakePort recipeAssetIntakePort(
            RecipeDocumentAssetStore recipeDocumentAssetStore,
            RecipeDocumentTextExtractor recipeDocumentTextExtractor,
            RecipeImageAssetStore recipeImageAssetStore,
            RecipeImageTextExtractor recipeImageTextExtractor
    ) {
        return new DocumentRecipeAssetIntakeService(
                recipeDocumentAssetStore,
                recipeDocumentTextExtractor,
                recipeImageAssetStore,
                recipeImageTextExtractor
        );
    }
}
