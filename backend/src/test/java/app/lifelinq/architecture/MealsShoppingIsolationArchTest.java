package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "app.lifelinq")
class MealsShoppingIsolationArchTest {

    @ArchTest
    static final ArchRule mealsMustNotDependOnShopping =
            noClasses().that().resideInAPackage("..features.meals..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .should().dependOnClassesThat().resideInAPackage("..features.shopping..");
}
