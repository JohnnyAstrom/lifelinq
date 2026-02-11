package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "app.lifelinq")
class FeatureInfrastructureIsolationArchTest {

    @ArchTest
    static final ArchRule featuresMustNotDependOnOtherFeatureInfrastructure =
            classes().that().resideInAPackage("..features..")
                    .should(new ArchCondition<>("not depend on other feature infrastructure") {
                        @Override
                        public void check(JavaClass javaClass, ConditionEvents events) {
                            String sourceFeature = featureName(javaClass.getPackage());
                            if (sourceFeature == null) {
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
                                if (!targetPackage.contains(".infrastructure")) {
                                    continue;
                                }

                                String targetFeature = featureName(target.getPackage());
                                if (targetFeature == null) {
                                    continue;
                                }
                                if (!sourceFeature.equals(targetFeature)) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on infrastructure of feature '"
                                                    + targetFeature + "' via " + target.getName()
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
