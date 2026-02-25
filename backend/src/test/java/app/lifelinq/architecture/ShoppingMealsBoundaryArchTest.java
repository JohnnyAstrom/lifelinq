package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "app.lifelinq")
class ShoppingMealsBoundaryArchTest {

    @ArchTest
    static final ArchRule shoppingMustNotDependOnMealsNonApplicationLayers =
            noClasses().that().resideInAPackage("..features.shopping..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..features.meals.domain..",
                            "..features.meals.api..",
                            "..features.meals.infrastructure..",
                            "..features.meals.contract.."
                    );
}
