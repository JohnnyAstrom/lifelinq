package app.lifelinq.features.group.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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
    static final ArchRule controllersShouldOnlyDependOnOwnFeatureApplication =
            classes().that().resideInAPackage("..features..api..")
                    .and().areAnnotatedWith(RestController.class)
                    .should(new ArchCondition<>("depend only on own feature application package and not on domain/infrastructure") {
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

                            String sameFeatureBase = "app.lifelinq.features." + feature + ".";
                            String sameFeatureApplication = sameFeatureBase + "application.";
                            String sameFeatureDomain = sameFeatureBase + "domain.";
                            String sameFeatureInfrastructure = sameFeatureBase + "infrastructure.";

                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (!targetPackage.startsWith("app.lifelinq.features.")) {
                                    continue;
                                }

                                boolean sameFeature = targetPackage.startsWith(sameFeatureBase);
                                boolean targetIsDomain = targetPackage.startsWith(sameFeatureDomain);
                                boolean targetIsInfrastructure = targetPackage.startsWith(sameFeatureInfrastructure);

                                if (sameFeature) {
                                    if (targetIsDomain || targetIsInfrastructure) {
                                        events.add(SimpleConditionEvent.violated(
                                                javaClass,
                                                javaClass.getName() + " depends on " + target.getName()
                                                        + " but controllers must not depend on own domain/infrastructure"
                                        ));
                                    }
                                    continue;
                                }

                                if (targetPackage.contains(".api.")
                                        || targetPackage.contains(".application.")
                                        || targetPackage.contains(".domain.")
                                        || targetPackage.contains(".infrastructure.")) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on " + target.getName()
                                                    + " but controllers must not depend on other features' api/application/domain/infrastructure"
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
