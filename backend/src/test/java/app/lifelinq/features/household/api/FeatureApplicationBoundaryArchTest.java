package app.lifelinq.features.household.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "app.lifelinq")
class FeatureApplicationBoundaryArchTest {

    @ArchTest
    static final ArchRule controllersShouldNotDependOnUseCases =
            noClasses().that().resideInAPackage("..features..api..")
                    .and().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule controllersShouldOnlyDependOnOwnApplicationService =
            classes().that().resideInAPackage("..features..api..")
                    .and().areAnnotatedWith(RestController.class)
                    .should(new ArchCondition<>("depend only on their own ApplicationService from application layer") {
                        @Override
                        public void check(JavaClass javaClass, ConditionEvents events) {
                            String feature = featureName(javaClass.getPackage());
                            if (feature == null) {
                                events.add(SimpleConditionEvent.violated(
                                        javaClass,
                                        "Could not determine feature name for " + javaClass.getName()
                                ));
                                return;
                            }

                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (!targetPackage.startsWith("app.lifelinq.features.")) {
                                    continue;
                                }
                                if (!targetPackage.contains(".application")) {
                                    continue;
                                }

                                boolean sameFeature = targetPackage.startsWith(
                                        "app.lifelinq.features." + feature + ".application"
                                );
                                boolean isApplicationService = target.getSimpleName().endsWith("ApplicationService");

                                if (!sameFeature || !isApplicationService) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on " + target.getName()
                                                    + " but controllers may only depend on their own ApplicationService"
                                    ));
                                }
                            }
                        }
                    });

    private static String featureName(JavaPackage javaPackage) {
        String name = javaPackage.getName();
        String marker = ".features.";
        int markerIndex = name.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }
        String remaining = name.substring(markerIndex + marker.length());
        int endIndex = remaining.indexOf('.');
        if (endIndex < 0) {
            return null;
        }
        return remaining.substring(0, endIndex);
    }
}
