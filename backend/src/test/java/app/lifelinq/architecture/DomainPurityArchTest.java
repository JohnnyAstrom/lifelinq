package app.lifelinq.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "app.lifelinq")
class DomainPurityArchTest {

    @ArchTest
    static final ArchRule domainShouldOnlyDependOnDomainCommonAndJava =
            classes().that().resideInAPackage("..features..domain..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .should(new ArchCondition<>("depend only on domain, common, and java packages") {
                        @Override
                        public void check(JavaClass javaClass, ConditionEvents events) {
                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (isAllowedDomainDependency(targetPackage)) {
                                    continue;
                                }
                                events.add(SimpleConditionEvent.violated(
                                        javaClass,
                                        javaClass.getName() + " depends on " + target.getName()
                                                + " which is outside domain/common/java"
                                ));
                            }
                        }
                    });

    @ArchTest
    static final ArchRule applicationShouldNotDependOnApiOrInfrastructure =
            classes().that().resideInAPackage("..features..application..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .and().haveSimpleNameNotEndingWith("Config")
                    .should(new ArchCondition<>("not depend on api or infrastructure") {
                        @Override
                        public void check(JavaClass javaClass, ConditionEvents events) {
                            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                                JavaClass target = dependency.getTargetClass();
                                String targetPackage = target.getPackageName();
                                if (targetPackage.contains(".features.") && targetPackage.contains(".api.")) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on API type " + target.getName()
                                    ));
                                }
                                if (targetPackage.contains(".features.") && targetPackage.contains(".infrastructure.")) {
                                    events.add(SimpleConditionEvent.violated(
                                            javaClass,
                                            javaClass.getName() + " depends on infrastructure type " + target.getName()
                                    ));
                                }
                                if (targetPackage.startsWith("org.springframework")) {
                                    if (!isAllowedTransactionalDependency(javaClass, target)) {
                                        events.add(SimpleConditionEvent.violated(
                                                javaClass,
                                                javaClass.getName() + " depends on Spring type " + target.getName()
                                        ));
                                    }
                                }
                            }
                        }
                    });

    @ArchTest
    static final ArchRule apiShouldNotDependOnInfrastructure =
            noClasses().that().resideInAPackage("..features..api..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("ArchTest")
                    .should().dependOnClassesThat().resideInAPackage("..features..infrastructure..");

    private static boolean isAllowedDomainDependency(String targetPackage) {
        return targetPackage.startsWith("java.")
                || targetPackage.startsWith("app.lifelinq.common.")
                || targetPackage.contains(".features.") && targetPackage.contains(".domain");
    }

    private static boolean isAllowedTransactionalDependency(JavaClass source, JavaClass target) {
        return source.getSimpleName().endsWith("ApplicationService")
                && target.getPackageName().equals("org.springframework.transaction.annotation");
    }
}
