package app.lifelinq.features.auth.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "app.lifelinq")
class AuthControllerBoundaryArchTest {

    @ArchTest
    static final ArchRule authRestControllersShouldDependOnlyOnAuthApplication =
            classes().that().resideInAPackage("..features.auth.api..")
                    .and().areAnnotatedWith(RestController.class)
                    .should(obeyAuthControllerDependencies());

    @ArchTest
    static final ArchRule authMvcControllersShouldDependOnlyOnAuthApplication =
            classes().that().resideInAPackage("..features.auth.api..")
                    .and().areAnnotatedWith(Controller.class)
                    .should(obeyAuthControllerDependencies())
                    .allowEmptyShould(true);

    private static ArchCondition<JavaClass> obeyAuthControllerDependencies() {
        return new ArchCondition<>("depend only on auth application/contract and not on config/domain/infrastructure") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    String targetPackage = target.getPackageName();

                    if (targetPackage.startsWith("app.lifelinq.config.")) {
                        events.add(SimpleConditionEvent.violated(
                                javaClass,
                                javaClass.getName() + " depends on config class " + target.getName()
                        ));
                        continue;
                    }

                    if (!targetPackage.startsWith("app.lifelinq.features.")) {
                        continue;
                    }

                    boolean allowed =
                            targetPackage.startsWith("app.lifelinq.features.auth.application")
                            || targetPackage.startsWith("app.lifelinq.features.auth.contract")
                            || targetPackage.startsWith("app.lifelinq.features.auth.api");

                    if (!allowed) {
                        events.add(SimpleConditionEvent.violated(
                                javaClass,
                                javaClass.getName() + " depends on " + target.getName()
                                        + " but auth controllers may depend only on auth application/contract"
                        ));
                    }
                }
            }
        };
    }
}
