package app.lifelinq.features.meals.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.AuthenticationFilter;
import app.lifelinq.config.GroupContextFilter;
import app.lifelinq.config.JwtVerifier;
import app.lifelinq.test.FakeActiveGroupUserRepository;
import app.lifelinq.features.meals.application.MealNotFoundException;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.application.RecipeDeleteBlockedException;
import app.lifelinq.features.meals.application.RecipeDuplicateAttentionRequiredException;
import app.lifelinq.features.meals.application.RecipeImportApplicationService;
import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.MealsShoppingAccessDeniedException;
import app.lifelinq.features.meals.contract.MealsShoppingDuplicateItemException;
import app.lifelinq.features.meals.contract.MealsShoppingListNotFoundException;
import app.lifelinq.features.meals.application.RecipeNotFoundException;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.RecipeImportDraftIngredientView;
import app.lifelinq.features.meals.contract.RecipeImportDraftView;
import app.lifelinq.features.meals.contract.RecipeDetailView;
import app.lifelinq.features.meals.contract.RecipeDraftView;
import app.lifelinq.features.meals.contract.HouseholdPreferenceSummaryView;
import app.lifelinq.features.meals.contract.IngredientCoverageView;
import app.lifelinq.features.meals.contract.MealChoiceCandidateView;
import app.lifelinq.features.meals.contract.MealIngredientNeedView;
import app.lifelinq.features.meals.contract.MealReadinessView;
import app.lifelinq.features.meals.contract.MealShoppingProjectionView;
import app.lifelinq.features.meals.contract.MealIdentitySummaryView;
import app.lifelinq.features.meals.contract.PlanningChoiceSupportView;
import app.lifelinq.features.meals.contract.AggregatedIngredientComparisonView;
import app.lifelinq.features.meals.contract.AggregatedIngredientNeedView;
import app.lifelinq.features.meals.contract.ContributorMealReferenceView;
import app.lifelinq.features.meals.contract.RecipeLibraryItemView;
import app.lifelinq.features.meals.contract.RecipeLifecycleView;
import app.lifelinq.features.meals.contract.RecipeProvenanceView;
import app.lifelinq.features.meals.contract.RecipeSourceView;
import app.lifelinq.features.meals.contract.RecentPlannedMealView;
import app.lifelinq.features.meals.contract.RecentMealOccurrenceView;
import app.lifelinq.features.meals.contract.RecipeUsageSummaryView;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.ShoppingDeltaView;
import app.lifelinq.features.meals.contract.ShoppingLinkReferenceView;
import app.lifelinq.features.meals.contract.WeekPlanView;
import app.lifelinq.features.meals.contract.WeekShoppingReviewLinkView;
import app.lifelinq.features.meals.contract.WeekShoppingReviewView;
import app.lifelinq.features.meals.contract.WeekShoppingProjectionView;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MealsControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private MealsApplicationService mealsApplicationService;
    private RecipeImportApplicationService recipeImportApplicationService;
    private FakeActiveGroupUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new FakeActiveGroupUserRepository();
        mealsApplicationService = Mockito.mock(MealsApplicationService.class);
        recipeImportApplicationService = Mockito.mock(RecipeImportApplicationService.class);
        MealsController controller = new MealsController(mealsApplicationService, recipeImportApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MealsExceptionHandler())
                .addFilters(
                        new AuthenticationFilter(new JwtVerifier(SECRET)),
                        new GroupContextFilter(userRepository)
                )
                .build();
    }

    @Test
    void addReturns401WhenTokenMissing() throws Exception {
        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + UUID.randomUUID() + "\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(mealsApplicationService);
    }

    @Test
    void addReturns200WithValidToken() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.addOrReplaceMeal(
                groupId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Pasta",
                recipeId,
                null,
                null
        )).thenReturn(new AddMealOutput(
                weekPlanId,
                2025,
                10,
                new PlannedMealView(1, "DINNER", recipeId, "Pasta", "Pasta", null, null)
        ));

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isOk());

        verify(mealsApplicationService).addOrReplaceMeal(
                groupId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Pasta",
                recipeId,
                null,
                null
        );
    }

    @Test
    void addAllowsLightweightMealTitleWithoutRecipe() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.addOrReplaceMeal(
                groupId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Tacos",
                null,
                null,
                null
        )).thenReturn(new AddMealOutput(
                weekPlanId,
                2025,
                10,
                new PlannedMealView(1, "DINNER", null, "Tacos", null, null, null)
        ));

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Tacos\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meal.mealTitle").value("Tacos"))
                .andExpect(jsonPath("$.meal.recipeTitle").value(org.hamcrest.Matchers.nullValue()));

        verify(mealsApplicationService).addOrReplaceMeal(
                groupId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Tacos",
                null,
                null,
                null
        );
    }

    @Test
    void removeReturns404WhenMealMissing() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new MealNotFoundException("Meal not found"))
                .when(mealsApplicationService)
                .removeMeal(groupId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER);

        mockMvc.perform(delete("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReturns403WhenShoppingListNotOwned() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new MealsShoppingAccessDeniedException("Access denied"))
                .when(mealsApplicationService)
                .addOrReplaceMeal(groupId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, "Pasta", recipeId, targetListId, null);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addReturns404WhenShoppingListMissing() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new MealsShoppingListNotFoundException("list not found: " + targetListId))
                .when(mealsApplicationService)
                .addOrReplaceMeal(groupId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, "Pasta", recipeId, targetListId, null);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReturns409WhenShoppingItemNameDuplicate() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new MealsShoppingDuplicateItemException("item name must be unique within list: pasta"))
                .when(mealsApplicationService)
                .addOrReplaceMeal(groupId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, "Pasta", recipeId, targetListId, null);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getWeekPlanIncludesReviewableWeekShoppingSignal() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getWeekPlan(groupId, userId, 2026, 13))
                .thenReturn(new WeekPlanView(
                        UUID.randomUUID(),
                        2026,
                        13,
                        Instant.parse("2026-03-26T10:00:00Z"),
                        true,
                        List.of()
                ));

        mockMvc.perform(get("/meals/weeks/2026/13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasReviewableWeekShopping").value(true));
    }

    @Test
    void getMealShoppingImpactReturnsProjectionFromProgram3Foundation() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getMealShoppingProjection(
                groupId,
                userId,
                2026,
                13,
                2,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                null
        )).thenReturn(new MealShoppingProjectionView(
                2026,
                13,
                2,
                "DINNER",
                "Creamy Pasta",
                recipeId,
                "Creamy Pasta",
                true,
                listId,
                "Main list",
                new ShoppingLinkReferenceView(listId, "Main list", Instant.parse("2026-03-24T10:00:00Z"), "linked"),
                new MealReadinessView("partially_ready", 2, 0, 0, 0, 1, 1),
                new ShoppingDeltaView(0, 0, 0, 0, List.of()),
                List.of(new IngredientCoverageView(
                        new MealIngredientNeedView(UUID.randomUUID(), 1, "Pasta", "pasta", null, null, null),
                        "covered",
                        "to_buy",
                        1,
                        null,
                        null,
                        null
                ))
        ));

        mockMvc.perform(get("/meals/weeks/2026/13/days/2/meals/DINNER/shopping-impact")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealTitle").value("Creamy Pasta"))
                .andExpect(jsonPath("$.assessedShoppingListId").value(listId.toString()))
                .andExpect(jsonPath("$.shoppingLink.status").value("linked"))
                .andExpect(jsonPath("$.readiness.state").value("partially_ready"));

        verify(mealsApplicationService).getMealShoppingProjection(
                groupId,
                userId,
                2026,
                13,
                2,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                null
        );
    }

    @Test
    void getMealShoppingImpactPassesSelectedShoppingListOverride() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getMealShoppingProjection(
                groupId,
                userId,
                2026,
                13,
                2,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                listId
        )).thenReturn(new MealShoppingProjectionView(
                2026,
                13,
                2,
                "DINNER",
                "Creamy Pasta",
                recipeId,
                "Creamy Pasta",
                true,
                listId,
                "Main list",
                new ShoppingLinkReferenceView(null, null, null, "not_linked"),
                new MealReadinessView("needs_shopping", 0, 0, 2, 0, 0, 2),
                new ShoppingDeltaView(2, 0, 2, 0, List.of()),
                List.of()
        ));

        mockMvc.perform(get("/meals/weeks/2026/13/days/2/meals/DINNER/shopping-impact")
                        .queryParam("shoppingListId", listId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assessedShoppingListId").value(listId.toString()))
                .andExpect(jsonPath("$.shoppingLink.status").value("not_linked"));

        verify(mealsApplicationService).getMealShoppingProjection(
                groupId,
                userId,
                2026,
                13,
                2,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                listId
        );
    }

    @Test
    void getWeekShoppingImpactReturnsAggregatedProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getWeekShoppingProjection(
                groupId,
                userId,
                2026,
                13
        )).thenReturn(new WeekShoppingProjectionView(
                UUID.randomUUID(),
                2026,
                13,
                1,
                0,
                0,
                1,
                new ShoppingDeltaView(1, 0, 1, 0, List.of()),
                List.of()
        ));

        mockMvc.perform(get("/meals/weeks/2026/13/shopping-impact")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealsNeedingShoppingCount").value(1))
                .andExpect(jsonPath("$.delta.unresolvedIngredientCount").value(1));

        verify(mealsApplicationService).getWeekShoppingProjection(groupId, userId, 2026, 13);
    }

    @Test
    void getWeekShoppingReviewReturnsProgram4FoundationProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getWeekShoppingReview(
                groupId,
                userId,
                2026,
                13,
                listId
        )).thenReturn(new WeekShoppingReviewView(
                weekPlanId,
                2026,
                13,
                listId,
                "Weekly groceries",
                new WeekShoppingReviewLinkView(
                        listId,
                        "Weekly groceries",
                        Instant.parse("2026-03-24T10:00:00Z")
                ),
                List.of(new AggregatedIngredientComparisonView(
                        new AggregatedIngredientNeedView(
                                "line-1",
                                "Egg",
                                "egg",
                                new java.math.BigDecimal("8"),
                                "PCS",
                                "exact",
                                List.of(new ContributorMealReferenceView(2, "DINNER", "Pancakes"))
                        ),
                        "add_to_list",
                        new java.math.BigDecimal("4"),
                        new java.math.BigDecimal("4")
                ))
        ));

        mockMvc.perform(get("/meals/weeks/2026/13/shopping-review")
                        .queryParam("shoppingListId", listId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekPlanId").value(weekPlanId.toString()))
                .andExpect(jsonPath("$.assessedShoppingListId").value(listId.toString()))
                .andExpect(jsonPath("$.assessedShoppingListName").value("Weekly groceries"))
                .andExpect(jsonPath("$.ingredients[0].comparisonState").value("add_to_list"))
                .andExpect(jsonPath("$.ingredients[0].need.quantityConfidence").value("exact"))
                .andExpect(jsonPath("$.ingredients[0].need.normalizedShoppingName").value("egg"));

        verify(mealsApplicationService).getWeekShoppingReview(groupId, userId, 2026, 13, listId);
    }

    @Test
    void addWeekShoppingReviewLinesReturnsUpdatedReviewProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.addWeekShoppingReviewLines(
                groupId,
                userId,
                2026,
                13,
                listId,
                List.of("line-1")
        )).thenReturn(new WeekShoppingReviewView(
                UUID.randomUUID(),
                2026,
                13,
                listId,
                "Weekly groceries",
                new WeekShoppingReviewLinkView(
                        listId,
                        "Weekly groceries",
                        Instant.parse("2026-03-24T10:00:00Z")
                ),
                List.of()
        ));

        mockMvc.perform(post("/meals/weeks/2026/13/shopping-review/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shoppingListId":"%s",
                                  "selectedLineIds":["line-1"]
                                }
                                """.formatted(listId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assessedShoppingListId").value(listId.toString()))
                .andExpect(jsonPath("$.reviewLink.shoppingListId").value(listId.toString()));

        verify(mealsApplicationService).addWeekShoppingReviewLines(
                groupId,
                userId,
                2026,
                13,
                listId,
                List.of("line-1")
        );
    }

    @Test
    void addReturns404WhenRecipeMissingInGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new RecipeNotFoundException(recipeId))
                .when(mealsApplicationService)
                .addOrReplaceMeal(groupId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, "Pasta", recipeId, null, null);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPassesSelectedIngredientPositions() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.addOrReplaceMeal(
                groupId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Pasta",
                recipeId,
                targetListId,
                List.of(1, 3)
        )).thenReturn(new AddMealOutput(
                weekPlanId,
                2025,
                10,
                new PlannedMealView(1, "DINNER", recipeId, "Pasta", "Pasta", null, null)
        ));

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTitle\":\"Pasta\",\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\",\"selectedIngredientPositions\":[1,3]}"))
                .andExpect(status().isOk());

        verify(mealsApplicationService).addOrReplaceMeal(
                groupId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Pasta",
                recipeId,
                targetListId,
                List.of(1, 3)
        );
    }

    @Test
    void createRecipePassesRecipeContentFields() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.createRecipe(
                groupId,
                userId,
                "Pasta",
                "Family notebook",
                "https://example.com/pasta",
                "URL_IMPORT",
                "4 servings",
                "Quick favorite",
                "Boil water\nCook pasta",
                null,
                List.of()
        )).thenReturn(new RecipeView(
                recipeId,
                groupId,
                "Pasta",
                "Family notebook",
                "https://example.com/pasta",
                "URL_IMPORT",
                "4 servings",
                "Quick favorite",
                "Boil water\nCook pasta",
                Instant.parse("2026-03-17T10:00:00Z"),
                Instant.parse("2026-03-17T10:15:00Z"),
                null,
                true,
                false,
                "Recipe must be archived before you can delete it.",
                List.of()
        ));

        mockMvc.perform(post("/meals/recipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Pasta",
                                  "sourceName":"Family notebook",
                                  "sourceUrl":"https://example.com/pasta",
                                  "originKind":"URL_IMPORT",
                                  "servings":"4 servings",
                                  "shortNote":"Quick favorite",
                                  "instructions":"Boil water\\nCook pasta",
                                  "ingredients":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceName").value("Family notebook"))
                .andExpect(jsonPath("$.sourceUrl").value("https://example.com/pasta"))
                .andExpect(jsonPath("$.originKind").value("URL_IMPORT"))
                .andExpect(jsonPath("$.servings").value("4 servings"))
                .andExpect(jsonPath("$.shortNote").value("Quick favorite"))
                .andExpect(jsonPath("$.instructions").value("Boil water\nCook pasta"))
                .andExpect(jsonPath("$.updatedAt").value("2026-03-17T10:15:00Z"));

        verify(mealsApplicationService).createRecipe(
                groupId,
                userId,
                "Pasta",
                "Family notebook",
                "https://example.com/pasta",
                "URL_IMPORT",
                "4 servings",
                "Quick favorite",
                "Boil water\nCook pasta",
                null,
                List.of()
        );
    }

    @Test
    void createManualRecipeDraftReturnsDraftProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.createManualRecipeDraft(groupId, userId))
                .thenReturn(new RecipeDraftView(
                        draftId,
                        groupId,
                        "draft_open",
                        null,
                        new RecipeSourceView(null, null),
                        new RecipeProvenanceView("manual", null),
                        null,
                        null,
                        null,
                        Instant.parse("2026-03-24T10:00:00Z"),
                        Instant.parse("2026-03-24T10:00:00Z"),
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipe-drafts/manual")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draftId").value(draftId.toString()))
                .andExpect(jsonPath("$.state").value("draft_open"))
                .andExpect(jsonPath("$.provenance.originKind").value("manual"));

        verify(mealsApplicationService).createManualRecipeDraft(groupId, userId);
    }

    @Test
    void updateRecipeDraftPassesScenarioPayload() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.updateRecipeDraft(
                groupId,
                userId,
                draftId,
                "Pasta",
                "Notebook",
                "https://example.com/pasta",
                "4 servings",
                "Quick favorite",
                "Cook",
                true,
                List.of()
        )).thenReturn(new RecipeDraftView(
                draftId,
                groupId,
                "draft_ready",
                "Pasta",
                new RecipeSourceView("Notebook", "https://example.com/pasta"),
                new RecipeProvenanceView("manual", "https://example.com/pasta"),
                "4 servings",
                "Quick favorite",
                "Cook",
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:05:00Z"),
                List.of()
        ));

        mockMvc.perform(put("/meals/recipe-drafts/" + draftId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Pasta",
                                  "sourceName":"Notebook",
                                  "sourceUrl":"https://example.com/pasta",
                                  "servings":"4 servings",
                                  "shortNote":"Quick favorite",
                                  "instructions":"Cook",
                                  "markReady":true,
                                  "ingredients":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("draft_ready"));

        verify(mealsApplicationService).updateRecipeDraft(
                groupId,
                userId,
                draftId,
                "Pasta",
                "Notebook",
                "https://example.com/pasta",
                "4 servings",
                "Quick favorite",
                "Cook",
                true,
                List.of()
        );
    }

    @Test
    void listRecipeLibraryItemsReturnsProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listRecipeLibraryItems(groupId, userId, "active"))
                .thenReturn(List.of(new RecipeLibraryItemView(
                        recipeId,
                        "Pasta",
                        new RecipeSourceView("Notebook", "https://example.com/pasta"),
                        new RecipeLifecycleView("active", false, "Recipe must be archived before you can delete it."),
                        Instant.parse("2026-03-24T09:00:00Z"),
                        Instant.parse("2026-03-24T10:00:00Z"),
                        3
                )));

        mockMvc.perform(get("/meals/recipe-library/items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeId").value(recipeId.toString()))
                .andExpect(jsonPath("$[0].lifecycle.state").value("active"))
                .andExpect(jsonPath("$[0].ingredientCount").value(3));

        verify(mealsApplicationService).listRecipeLibraryItems(groupId, userId, "active");
    }

    @Test
    void listRecentRecipeLibraryItemsReturnsPlatformProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listRecentlyUsedRecipeLibraryItems(groupId, userId))
                .thenReturn(List.of(new RecipeLibraryItemView(
                        recipeId,
                        "Recent Pasta",
                        new RecipeSourceView("Notebook", null),
                        new RecipeLifecycleView("active", false, "Recipe must be archived before you can delete it."),
                        Instant.parse("2026-03-24T09:00:00Z"),
                        Instant.parse("2026-03-24T10:00:00Z"),
                        3
                )));

        mockMvc.perform(get("/meals/recipe-library/recent-items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Recent Pasta"))
                .andExpect(jsonPath("$[0].lifecycle.state").value("active"));

        verify(mealsApplicationService).listRecentlyUsedRecipeLibraryItems(groupId, userId);
    }

    @Test
    void listArchivedRecipeLibraryItemsUsesArchivedPlatformState() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listRecipeLibraryItems(groupId, userId, "archived"))
                .thenReturn(List.of(new RecipeLibraryItemView(
                        recipeId,
                        "Archived Pasta",
                        new RecipeSourceView("Notebook", null),
                        new RecipeLifecycleView("archived", true, null),
                        null,
                        Instant.parse("2026-03-24T10:00:00Z"),
                        2
                )));

        mockMvc.perform(get("/meals/recipe-library/items?state=archived")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Archived Pasta"))
                .andExpect(jsonPath("$[0].lifecycle.state").value("archived"));

        verify(mealsApplicationService).listRecipeLibraryItems(groupId, userId, "archived");
    }

    @Test
    void getRecipeDetailReturnsPlatformProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getRecipeDetail(groupId, userId, recipeId))
                .thenReturn(new RecipeDetailView(
                        recipeId,
                        groupId,
                        "Pasta",
                        new RecipeSourceView("Notebook", "https://example.com/pasta"),
                        new RecipeProvenanceView("manual", null),
                        new RecipeLifecycleView("active", false, "Recipe must be archived before you can delete it."),
                        "4 servings",
                        null,
                        "Quick favorite",
                        "Cook",
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-24T10:00:00Z"),
                        true,
                        List.of()
                ));

        mockMvc.perform(get("/meals/recipe-details/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(recipeId.toString()))
                .andExpect(jsonPath("$.source.sourceName").value("Notebook"))
                .andExpect(jsonPath("$.provenance.originKind").value("manual"))
                .andExpect(jsonPath("$.lifecycle.state").value("active"));

        verify(mealsApplicationService).getRecipeDetail(groupId, userId, recipeId);
    }

    @Test
    void updateRecipeDetailReturnsPlatformProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getRecipeDetail(groupId, userId, recipeId))
                .thenReturn(new RecipeDetailView(
                        recipeId,
                        groupId,
                        "Pasta",
                        new RecipeSourceView("Notebook", "https://example.com/pasta"),
                        new RecipeProvenanceView("manual", null),
                        new RecipeLifecycleView("active", false, "Recipe must be archived before you can delete it."),
                        "4 servings",
                        null,
                        "Quick favorite",
                        "Cook",
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-24T10:00:00Z"),
                        true,
                        List.of()
                ));

        mockMvc.perform(put("/meals/recipe-details/" + recipeId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Pasta",
                                  "sourceName":"Notebook",
                                  "sourceUrl":"https://example.com/pasta",
                                  "originKind":"MANUAL",
                                  "servings":"4 servings",
                                  "shortNote":"Quick favorite",
                                  "instructions":"Cook",
                                  "ingredients":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycle.state").value("active"))
                .andExpect(jsonPath("$.source.sourceUrl").value("https://example.com/pasta"));

        verify(mealsApplicationService).updateRecipe(
                groupId,
                userId,
                recipeId,
                "Pasta",
                "Notebook",
                "https://example.com/pasta",
                "MANUAL",
                "4 servings",
                "Quick favorite",
                "Cook",
                null,
                List.of()
        );
        verify(mealsApplicationService).getRecipeDetail(groupId, userId, recipeId);
    }

    @Test
    void acceptRecipeDraftReturns409WhenDuplicateAttentionIsRequired() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new RecipeDuplicateAttentionRequiredException("This recipe link is already saved in your library."))
                .when(mealsApplicationService)
                .acceptRecipeDraft(groupId, userId, draftId, false);

        mockMvc.perform(post("/meals/recipe-drafts/" + draftId + "/accept")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RECIPE_DUPLICATE_ATTENTION_REQUIRED"));
    }

    @Test
    void archiveRecipeReturnsArchivedRecipeResponse() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.archiveRecipe(groupId, userId, recipeId))
                .thenReturn(new RecipeView(
                        recipeId,
                        groupId,
                        "Soup",
                        "Notebook",
                        null,
                        "MANUAL",
                        null,
                        null,
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-18T10:00:00Z"),
                        Instant.parse("2026-03-18T10:00:00Z"),
                        true,
                        true,
                        null,
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipes/" + recipeId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(recipeId.toString()))
                .andExpect(jsonPath("$.archivedAt").value("2026-03-18T10:00:00Z"));

        verify(mealsApplicationService).archiveRecipe(groupId, userId, recipeId);
    }

    @Test
    void archiveRecipeDetailReturnsArchivedPlatformProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getRecipeDetail(groupId, userId, recipeId))
                .thenReturn(new RecipeDetailView(
                        recipeId,
                        groupId,
                        "Soup",
                        new RecipeSourceView("Notebook", null),
                        new RecipeProvenanceView("manual", null),
                        new RecipeLifecycleView("archived", true, null),
                        null,
                        null,
                        null,
                        null,
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-18T10:00:00Z"),
                        true,
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipe-details/" + recipeId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycle.state").value("archived"))
                .andExpect(jsonPath("$.lifecycle.deleteEligible").value(true));

        verify(mealsApplicationService).archiveRecipe(groupId, userId, recipeId);
        verify(mealsApplicationService).getRecipeDetail(groupId, userId, recipeId);
    }

    @Test
    void listArchivedRecipesReturnsArchivedRecipeResponses() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listArchivedRecipes(groupId, userId))
                .thenReturn(List.of(new RecipeView(
                        recipeId,
                        groupId,
                        "Archived Soup",
                        "Notebook",
                        null,
                        "MANUAL",
                        null,
                        null,
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-18T10:00:00Z"),
                        Instant.parse("2026-03-18T10:00:00Z"),
                        true,
                        false,
                        "This recipe is still used in planned meals.",
                        List.of()
                )));

        mockMvc.perform(get("/meals/recipes/archived")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Archived Soup"))
                .andExpect(jsonPath("$[0].archivedAt").value("2026-03-18T10:00:00Z"));

        verify(mealsApplicationService).listArchivedRecipes(groupId, userId);
    }

    @Test
    void listRecentlyUsedRecipesReturnsRecipeResponses() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listRecentlyUsedRecipes(groupId, userId))
                .thenReturn(List.of(new RecipeView(
                        recipeId,
                        groupId,
                        "Recent Pasta",
                        "Koket",
                        null,
                        "MANUAL",
                        null,
                        null,
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-18T10:00:00Z"),
                        null,
                        true,
                        false,
                        "Recipe must be archived before you can delete it.",
                        List.of()
                )));

        mockMvc.perform(get("/meals/recipes/recently-used")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Recent Pasta"));

        verify(mealsApplicationService).listRecentlyUsedRecipes(groupId, userId);
    }

    @Test
    void listRecentPlannedMealsReturnsMealResponses() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listRecentPlannedMeals(groupId, userId))
                .thenReturn(List.of(new RecentPlannedMealView(
                        2026,
                        12,
                        5,
                        "DINNER",
                        "Recent Pasta",
                        recipeId,
                        "Pasta Bake"
                )));

        mockMvc.perform(get("/meals/recently-planned")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mealTitle").value("Recent Pasta"))
                .andExpect(jsonPath("$[0].mealType").value("DINNER"))
                .andExpect(jsonPath("$[0].recipeId").value(recipeId.toString()));

        verify(mealsApplicationService).listRecentPlannedMeals(groupId, userId);
    }

    @Test
    void listRecentMealOccurrencesReturnsProgram2Projection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.listRecentMealOccurrences(groupId, userId, 5))
                .thenReturn(List.of(new RecentMealOccurrenceView(
                        weekPlanId,
                        2026,
                        12,
                        5,
                        "DINNER",
                        java.time.LocalDate.parse("2026-03-20"),
                        "Tacos",
                        "title:tacos",
                        "title_only",
                        null,
                        null
                )));

        mockMvc.perform(get("/meals/household-memory/recent-occurrences?limit=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mealTitle").value("Tacos"))
                .andExpect(jsonPath("$[0].mealIdentityKind").value("title_only"));

        verify(mealsApplicationService).listRecentMealOccurrences(groupId, userId, 5);
    }

    @Test
    void writeHouseholdPreferenceSignalReturnsProgram2PreferenceSummary() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID signalId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.writeHouseholdPreferenceSignal(
                groupId,
                userId,
                "recipe",
                "prefer",
                recipeId,
                null
        )).thenReturn(new HouseholdPreferenceSummaryView(
                signalId,
                "recipe",
                recipeId,
                null,
                "prefer",
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:00:00Z")
        ));

        mockMvc.perform(post("/meals/household-memory/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetKind":"recipe",
                                  "signalType":"prefer",
                                  "recipeId":"%s"
                                }
                                """.formatted(recipeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetKind").value("recipe"))
                .andExpect(jsonPath("$.signalType").value("prefer"));

        verify(mealsApplicationService).writeHouseholdPreferenceSignal(
                groupId,
                userId,
                "recipe",
                "prefer",
                recipeId,
                null
        );
    }

    @Test
    void getSlotPlanningChoiceSupportReturnsGroupedCandidates() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getSlotPlanningChoiceSupport(
                groupId,
                userId,
                2026,
                13,
                2,
                app.lifelinq.features.meals.domain.MealType.DINNER
        )).thenReturn(new PlanningChoiceSupportView(
                "slot",
                java.time.LocalDate.parse("2026-03-24"),
                2026,
                13,
                2,
                "DINNER",
                null,
                List.of(new MealChoiceCandidateView(
                        "recent",
                        "title:tacos",
                        "title_only",
                        "Tacos",
                        null,
                        java.time.LocalDate.parse("2026-03-23"),
                        2,
                        true,
                        false,
                        true,
                        false,
                        true,
                        false,
                        false,
                        false,
                        "planned recently"
                )),
                List.of(),
                List.of(),
                List.of()
        ));

        mockMvc.perform(get("/meals/choice-support/slot?year=2026&isoWeek=13&dayOfWeek=2&mealType=DINNER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenario").value("slot"))
                .andExpect(jsonPath("$.recentCandidates[0].title").value("Tacos"))
                .andExpect(jsonPath("$.recentCandidates[0].slotFit").value(true));

        verify(mealsApplicationService).getSlotPlanningChoiceSupport(
                groupId,
                userId,
                2026,
                13,
                2,
                app.lifelinq.features.meals.domain.MealType.DINNER
        );
    }

    @Test
    void markRecipeMakeSoonReturnsUpdatedRecipeResponse() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.markRecipeMakeSoon(groupId, userId, recipeId))
                .thenReturn(new RecipeView(
                        recipeId,
                        groupId,
                        "Soon Pasta",
                        "Notebook",
                        null,
                        "MANUAL",
                        "4 servings",
                        Instant.parse("2026-03-21T09:00:00Z"),
                        "Quick dinner",
                        "Cook",
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-21T09:00:00Z"),
                        null,
                        true,
                        false,
                        "Recipe must be archived before you can delete it.",
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipes/" + recipeId + "/make-soon")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.makeSoonAt").value("2026-03-21T09:00:00Z"));

        verify(mealsApplicationService).markRecipeMakeSoon(groupId, userId, recipeId);
    }

    @Test
    void markRecipeDetailMakeSoonReturnsPlatformProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getRecipeDetail(groupId, userId, recipeId))
                .thenReturn(new RecipeDetailView(
                        recipeId,
                        groupId,
                        "Soon Pasta",
                        new RecipeSourceView("Notebook", null),
                        new RecipeProvenanceView("manual", null),
                        new RecipeLifecycleView("active", false, "Recipe must be archived before you can delete it."),
                        "4 servings",
                        Instant.parse("2026-03-21T09:00:00Z"),
                        "Quick dinner",
                        "Cook",
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-21T09:00:00Z"),
                        true,
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipe-details/" + recipeId + "/make-soon")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.makeSoonAt").value("2026-03-21T09:00:00Z"))
                .andExpect(jsonPath("$.lifecycle.state").value("active"));

        verify(mealsApplicationService).markRecipeMakeSoon(groupId, userId, recipeId);
        verify(mealsApplicationService).getRecipeDetail(groupId, userId, recipeId);
    }

    @Test
    void clearRecipeMakeSoonReturnsUpdatedRecipeResponse() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.clearRecipeMakeSoon(groupId, userId, recipeId))
                .thenReturn(new RecipeView(
                        recipeId,
                        groupId,
                        "Soon Pasta",
                        "Notebook",
                        null,
                        "MANUAL",
                        "4 servings",
                        null,
                        "Quick dinner",
                        "Cook",
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-22T09:00:00Z"),
                        null,
                        true,
                        false,
                        "Recipe must be archived before you can delete it.",
                        List.of()
                ));

        mockMvc.perform(delete("/meals/recipes/" + recipeId + "/make-soon")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.makeSoonAt").value(org.hamcrest.Matchers.nullValue()));

        verify(mealsApplicationService).clearRecipeMakeSoon(groupId, userId, recipeId);
    }

    @Test
    void restoreRecipeReturnsActiveRecipeResponse() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.restoreRecipe(groupId, userId, recipeId))
                .thenReturn(new RecipeView(
                        recipeId,
                        groupId,
                        "Restored Soup",
                        "Notebook",
                        null,
                        "MANUAL",
                        null,
                        null,
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-20T10:00:00Z"),
                        null,
                        true,
                        false,
                        "Recipe must be archived before you can delete it.",
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipes/" + recipeId + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Restored Soup"))
                .andExpect(jsonPath("$.archivedAt").value(org.hamcrest.Matchers.nullValue()));

        verify(mealsApplicationService).restoreRecipe(groupId, userId, recipeId);
    }

    @Test
    void restoreRecipeDetailReturnsActivePlatformProjection() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.getRecipeDetail(groupId, userId, recipeId))
                .thenReturn(new RecipeDetailView(
                        recipeId,
                        groupId,
                        "Restored Soup",
                        new RecipeSourceView("Notebook", null),
                        new RecipeProvenanceView("manual", null),
                        new RecipeLifecycleView("active", false, "Recipe must be archived before you can delete it."),
                        null,
                        null,
                        null,
                        null,
                        Instant.parse("2026-03-10T09:00:00Z"),
                        Instant.parse("2026-03-20T10:00:00Z"),
                        true,
                        List.of()
                ));

        mockMvc.perform(post("/meals/recipe-details/" + recipeId + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Restored Soup"))
                .andExpect(jsonPath("$.lifecycle.state").value("active"));

        verify(mealsApplicationService).restoreRecipe(groupId, userId, recipeId);
        verify(mealsApplicationService).getRecipeDetail(groupId, userId, recipeId);
    }

    @Test
    void deleteRecipeReturns204WhenAllowed() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(delete("/meals/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(mealsApplicationService).deleteRecipe(groupId, userId, recipeId);
    }

    @Test
    void deleteRecipeReturns409WhenBlocked() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new RecipeDeleteBlockedException("This recipe is still used in planned meals."))
                .when(mealsApplicationService)
                .deleteRecipe(groupId, userId, recipeId);

        mockMvc.perform(delete("/meals/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RECIPE_DELETE_BLOCKED"))
                .andExpect(jsonPath("$.message").value("This recipe is still used in planned meals."));
    }

    @Test
    void createRecipeImportDraftReturnsNormalizedDraft() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(recipeImportApplicationService.importRecipeDraft(
                groupId,
                userId,
                "https://example.com/pie"
        )).thenReturn(new RecipeImportDraftView(
                "Apple Pie",
                "Example Kitchen",
                "https://example.com/pie",
                "URL_IMPORT",
                "8 slices",
                "Weekend dessert",
                "Mix ingredients\nBake",
                List.of(
                        new RecipeImportDraftIngredientView("apple", null, null, 1),
                        new RecipeImportDraftIngredientView("milk", java.math.BigDecimal.ONE, app.lifelinq.features.meals.contract.IngredientUnitView.DL, 2)
                )
        ));

        mockMvc.perform(post("/meals/recipes/import-drafts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url":"https://example.com/pie"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple Pie"))
                .andExpect(jsonPath("$.sourceName").value("Example Kitchen"))
                .andExpect(jsonPath("$.sourceUrl").value("https://example.com/pie"))
                .andExpect(jsonPath("$.originKind").value("URL_IMPORT"))
                .andExpect(jsonPath("$.servings").value("8 slices"))
                .andExpect(jsonPath("$.ingredients[1].quantity").value(1))
                .andExpect(jsonPath("$.ingredients[1].unit").value("DL"));

        verify(recipeImportApplicationService).importRecipeDraft(groupId, userId, "https://example.com/pie");
    }

    @Test
    void createRecipeImportDraftReturns422WhenImportFails() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(recipeImportApplicationService.importRecipeDraft(
                groupId,
                userId,
                "https://example.com/broken"
        )).thenThrow(new RecipeImportFailedException("Could not find structured recipe data at that URL"));

        mockMvc.perform(post("/meals/recipes/import-drafts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url":"https://example.com/broken"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("RECIPE_IMPORT_FAILED"));
    }

    private String createToken(UUID userId, Instant exp) throws Exception {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"userId\":\"%s\",\"exp\":%d}",
                userId,
                exp.getEpochSecond()
        );
        String headerPart = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signaturePart = base64Url(hmacSha256(headerPart + "." + payloadPart));
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    private byte[] hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

}
