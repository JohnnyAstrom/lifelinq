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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "app.lifelinq")
class FeatureApplicationBoundaryArchTest {

    @ArchTest
    static final ArchRule restControllersShouldOnlyDependOnOwnFeatureApplication =
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

                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (!targetPackage.startsWith("app.lifelinq.features.")) {
                                    continue;
                                }

                                boolean sameFeature = targetPackage.startsWith(sameFeatureBase);
                                boolean targetIsInfrastructure = isSameFeatureLayer(targetPackage, sameFeatureBase, "infrastructure");

                                if (sameFeature) {
                                    if (targetIsInfrastructure) {
                                        events.add(SimpleConditionEvent.violated(
                                                javaClass,
                                                javaClass.getName() + " depends on " + target.getName()
                                                        + " but controllers must not depend on own infrastructure"
                                        ));
                                    }
                                    continue;
                                }

                                if (isFeatureLayer(targetPackage, "api")
                                        || isFeatureLayer(targetPackage, "application")
                                        || isFeatureLayer(targetPackage, "domain")
                                        || isFeatureLayer(targetPackage, "infrastructure")) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on " + target.getName()
                                                    + " but controllers must not depend on other features' api/application/domain/infrastructure"
                                    ));
                                }

                            }
                        }
                    });

    @ArchTest
    static final ArchRule mvcControllersShouldOnlyDependOnOwnFeatureApplication =
            classes().that().resideInAPackage("..features..api..")
                    .and().areAnnotatedWith(Controller.class)
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

                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (!targetPackage.startsWith("app.lifelinq.features.")) {
                                    continue;
                                }

                                boolean sameFeature = targetPackage.startsWith(sameFeatureBase);
                                boolean targetIsInfrastructure = isSameFeatureLayer(targetPackage, sameFeatureBase, "infrastructure");

                                if (sameFeature) {
                                    if (targetIsInfrastructure) {
                                        events.add(SimpleConditionEvent.violated(
                                                javaClass,
                                                javaClass.getName() + " depends on " + target.getName()
                                                        + " but controllers must not depend on own infrastructure"
                                        ));
                                    }
                                    continue;
                                }

                                if (isFeatureLayer(targetPackage, "api")
                                        || isFeatureLayer(targetPackage, "application")
                                        || isFeatureLayer(targetPackage, "domain")
                                        || isFeatureLayer(targetPackage, "infrastructure")) {
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

    private static boolean isSameFeatureLayer(String packageName, String sameFeatureBase, String layer) {
        String root = sameFeatureBase + layer;
        return packageName.equals(root) || packageName.startsWith(root + ".");
    }

    private static boolean isFeatureLayer(String packageName, String layer) {
        String marker = "." + layer + ".";
        return packageName.contains(marker) || packageName.endsWith("." + layer);
    }
}
