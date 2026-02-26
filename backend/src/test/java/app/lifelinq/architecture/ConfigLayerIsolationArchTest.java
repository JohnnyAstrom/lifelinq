package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "app.lifelinq")
class ConfigLayerIsolationArchTest {

    @ArchTest
    static final ArchRule phase2FiltersMustNotDependOnFeatureApplication =
            noClasses().that().haveFullyQualifiedName("app.lifelinq.config.AuthenticationFilter")
                    .or().haveFullyQualifiedName("app.lifelinq.config.GroupContextFilter")
                    .should().dependOnClassesThat().resideInAPackage("..features..application..");
}
