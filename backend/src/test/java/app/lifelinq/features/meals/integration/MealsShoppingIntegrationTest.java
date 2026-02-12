package app.lifelinq.features.meals.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.contract.CreateShoppingListOutput;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
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
    void addMealPushesShoppingItemToList() {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        householdRepository.save(new Household(householdId, "Home"));
        membershipRepository.save(new Membership(householdId, userId, HouseholdRole.OWNER));

        CreateShoppingListOutput listOutput = shoppingApplicationService.createShoppingList(
                householdId,
                userId,
                "Groceries"
        );

        UUID recipeId = UUID.randomUUID();
        mealsApplicationService.addOrReplaceMeal(
                householdId,
                userId,
                2025,
                10,
                1,
                recipeId,
                "Pasta",
                listOutput.listId()
        );

        ShoppingList list = shoppingListRepository.findById(listOutput.listId()).orElseThrow();
        assertEquals(1, list.getItems().size());
        assertTrue(list.getItems().get(0).getName().equals("pasta"));
    }
}
