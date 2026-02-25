package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "app.lifelinq")
class UserApplicationIsolationArchTest {

    @ArchTest
    static final ArchRule nonUserFeaturesMustNotDependOnUserApplication =
            noClasses().that().resideInAPackage("..features..")
                    .and().resideOutsideOfPackage("..features.user..")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .should().dependOnClassesThat().resideInAPackage("..features.user.application..");
}
