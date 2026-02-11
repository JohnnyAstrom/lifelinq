package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "app.lifelinq")
class RequestContextUsageArchTest {

    @ArchTest
    static final ArchRule requestContextMustStayInApi =
            noClasses().that().resideInAnyPackage(
                            "..features..application..",
                            "..features..domain..",
                            "..features..infrastructure.."
                    )
                    .should().dependOnClassesThat().haveSimpleNameContaining("RequestContext");
}
