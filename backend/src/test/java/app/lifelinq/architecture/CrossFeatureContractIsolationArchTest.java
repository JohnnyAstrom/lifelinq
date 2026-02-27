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
class CrossFeatureContractIsolationArchTest {

    @ArchTest
    static final ArchRule crossFeatureDependenciesMustGoThroughContract =
            classes().that().resideInAPackage("..features..")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .should(new ArchCondition<>("depend on other features only via contract") {
                        @Override
                        public void check(JavaClass javaClass, ConditionEvents events) {
                            String sourceFeature = featureName(javaClass.getPackage());
                            if (sourceFeature == null) {
                                return;
                            }

                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (!targetPackage.startsWith("app.lifelinq.features.")) {
                                    continue;
                                }

                                String targetFeature = featureName(target.getPackage());
                                if (targetFeature == null || sourceFeature.equals(targetFeature)) {
                                    continue;
                                }

                                if (targetPackage.contains(".contract.")) {
                                    continue;
                                }
                                if (targetPackage.contains(".application.")
                                        || targetPackage.contains(".domain.")
                                        || targetPackage.contains(".infrastructure.")
                                        || targetPackage.contains(".api.")) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on " + target.getName()
                                                    + " in feature '" + targetFeature
                                                    + "' outside contract package"
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
