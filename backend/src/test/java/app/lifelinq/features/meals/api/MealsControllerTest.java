package app.lifelinq.features.meals.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.AuthenticationFilter;
import app.lifelinq.config.GroupContextFilter;
import app.lifelinq.config.JwtVerifier;
import app.lifelinq.test.FakeActiveGroupUserRepository;
import app.lifelinq.features.meals.application.MealNotFoundException;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.application.RecipeDeleteBlockedException;
import app.lifelinq.features.meals.application.RecipeImportApplicationService;
import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.MealsShoppingAccessDeniedException;
import app.lifelinq.features.meals.contract.MealsShoppingDuplicateItemException;
import app.lifelinq.features.meals.contract.MealsShoppingListNotFoundException;
import app.lifelinq.features.meals.application.RecipeNotFoundException;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.RecipeImportDraftIngredientView;
import app.lifelinq.features.meals.contract.RecipeImportDraftView;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.contract.PlannedMealView;
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
                new PlannedMealView(1, "DINNER", recipeId, "Pasta", "Pasta")
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
                new PlannedMealView(1, "DINNER", null, "Tacos", null)
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
                new PlannedMealView(1, "DINNER", recipeId, "Pasta", "Pasta")
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
                                  "shortNote":"Quick favorite",
                                  "instructions":"Boil water\\nCook pasta",
                                  "ingredients":[]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceName").value("Family notebook"))
                .andExpect(jsonPath("$.sourceUrl").value("https://example.com/pasta"))
                .andExpect(jsonPath("$.originKind").value("URL_IMPORT"))
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
                "Quick favorite",
                "Boil water\nCook pasta",
                null,
                List.of()
        );
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
