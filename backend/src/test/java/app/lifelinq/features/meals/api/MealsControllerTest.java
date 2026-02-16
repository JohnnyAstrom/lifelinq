package app.lifelinq.features.meals.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.household.application.HouseholdApplicationServiceTestFactory;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.meals.application.MealNotFoundException;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.application.RecipeNotFoundException;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.shopping.domain.DuplicateShoppingItemNameException;
import app.lifelinq.features.shopping.domain.ShoppingListNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private FakeMembershipRepository membershipRepository;

    @BeforeEach
    void setUp() {
        membershipRepository = new FakeMembershipRepository();
        mealsApplicationService = Mockito.mock(MealsApplicationService.class);
        MealsController controller = new MealsController(mealsApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MealsExceptionHandler())
                .addFilters(new RequestContextFilter(
                        new JwtVerifier(SECRET),
                        HouseholdApplicationServiceTestFactory.createForContextResolution(membershipRepository)
                ))
                .build();
    }

    @Test
    void addReturns401WhenTokenMissing() throws Exception {
        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + UUID.randomUUID() + "\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(mealsApplicationService);
    }

    @Test
    void addReturns200WithValidToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(mealsApplicationService.addOrReplaceMeal(
                householdId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                recipeId,
                null
        )).thenReturn(new AddMealOutput(
                weekPlanId,
                2025,
                10,
                new PlannedMealView(1, "DINNER", recipeId, "Pasta")
        ));

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isOk());

        verify(mealsApplicationService).addOrReplaceMeal(
                householdId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                recipeId,
                null
        );
    }

    @Test
    void removeReturns404WhenMealMissing() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new MealNotFoundException("Meal not found"))
                .when(mealsApplicationService)
                .removeMeal(householdId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER);

        mockMvc.perform(delete("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReturns403WhenShoppingListNotOwned() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new app.lifelinq.features.shopping.application.AccessDeniedException("Access denied"))
                .when(mealsApplicationService)
                .addOrReplaceMeal(householdId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, recipeId, targetListId);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addReturns404WhenShoppingListMissing() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new ShoppingListNotFoundException(targetListId))
                .when(mealsApplicationService)
                .addOrReplaceMeal(householdId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, recipeId, targetListId);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReturns409WhenShoppingItemNameDuplicate() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID targetListId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new DuplicateShoppingItemNameException("pasta"))
                .when(mealsApplicationService)
                .addOrReplaceMeal(householdId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, recipeId, targetListId);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\",\"targetShoppingListId\":\"" + targetListId + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void addReturns404WhenRecipeMissingInHousehold() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Mockito.doThrow(new RecipeNotFoundException(recipeId))
                .when(mealsApplicationService)
                .addOrReplaceMeal(householdId, userId, 2025, 10, 1, app.lifelinq.features.meals.domain.MealType.DINNER, recipeId, null);

        mockMvc.perform(post("/meals/weeks/2025/10/days/1/meals/DINNER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + recipeId + "\",\"mealType\":\"DINNER\"}"))
                .andExpect(status().isNotFound());
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

    private static final class FakeMembershipRepository implements MembershipRepository {
        private final Map<UUID, List<UUID>> byUser = new HashMap<>();

        FakeMembershipRepository withMembership(UUID userId, UUID householdId) {
            byUser.put(userId, List.of(householdId));
            return this;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            return byUser.getOrDefault(userId, List.of());
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
