package app.lifelinq.features.meals.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.contract.CreateShoppingListOutput;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import app.lifelinq.test.integration.MealsShoppingIntegrationTestApplication;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MealsShoppingIntegrationTestApplication.class)
@ActiveProfiles({"test", "persistence"})
class MealsShoppingIntegrationTest {

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingApplicationService shoppingApplicationService;

    @Autowired
    private MealsApplicationService mealsApplicationService;

    @Test
    void addMealPushesIngredientsToList() {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        householdRepository.save(new Household(householdId, "Home"));
        membershipRepository.save(new Membership(householdId, userId, HouseholdRole.OWNER));

        CreateShoppingListOutput listOutput = shoppingApplicationService.createShoppingList(
                householdId,
                userId,
                "Groceries"
        );

        RecipeView recipe = mealsApplicationService.createRecipe(
                householdId,
                userId,
                "Pasta",
                List.of(
                        new IngredientInput("  Olive   Oil  ", new BigDecimal("2"), IngredientUnit.DL, 1),
                        new IngredientInput("Tomato", null, null, 2)
                )
        );

        mealsApplicationService.addOrReplaceMeal(
                householdId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                recipe.recipeId(),
                listOutput.listId()
        );

        ShoppingList list = shoppingListRepository.findById(listOutput.listId()).orElseThrow();
        assertEquals(2, list.getItems().size());
        assertEquals("olive oil", list.getItems().get(0).getName());
        assertEquals(0, list.getItems().get(0).getQuantity().compareTo(new BigDecimal("2")));
        assertEquals(ShoppingUnit.DL, list.getItems().get(0).getUnit());
        assertEquals("tomato", list.getItems().get(1).getName());
    }

    @Test
    void duplicateIngredientNamesCreateSeparateItems() {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        householdRepository.save(new Household(householdId, "Home"));
        membershipRepository.save(new Membership(householdId, userId, HouseholdRole.OWNER));

        CreateShoppingListOutput listOutput = shoppingApplicationService.createShoppingList(
                householdId,
                userId,
                "Groceries"
        );

        RecipeView recipe = mealsApplicationService.createRecipe(
                householdId,
                userId,
                "Salad",
                List.of(
                        new IngredientInput("Tomato", null, null, 1),
                        new IngredientInput("tomato", null, null, 2)
                )
        );

        mealsApplicationService.addOrReplaceMeal(
                householdId,
                userId,
                2025,
                10,
                1,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                recipe.recipeId(),
                listOutput.listId()
        );

        ShoppingList list = shoppingListRepository.findById(listOutput.listId()).orElseThrow();
        assertEquals(2, list.getItems().size());
        assertTrue(list.getItems().stream().allMatch(item -> item.getName().equals("tomato")));
    }
}
