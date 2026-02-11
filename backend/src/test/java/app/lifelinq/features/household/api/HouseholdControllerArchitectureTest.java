package app.lifelinq.features.household.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.application.HouseholdApplicationService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HouseholdControllerArchitectureTest {

    @Test
    void controllerDependsOnlyOnApplicationServiceFromApplicationLayer() {
        Set<Class<?>> referencedTypes = new HashSet<>();
        Class<?> controllerClass = HouseholdController.class;

        for (Field field : controllerClass.getDeclaredFields()) {
            referencedTypes.add(field.getType());
        }
        for (Constructor<?> constructor : controllerClass.getDeclaredConstructors()) {
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                referencedTypes.add(parameterType);
            }
        }
        for (Method method : controllerClass.getDeclaredMethods()) {
            referencedTypes.add(method.getReturnType());
            for (Class<?> parameterType : method.getParameterTypes()) {
                referencedTypes.add(parameterType);
            }
        }

        for (Class<?> type : referencedTypes) {
            if (type.getName().startsWith("app.lifelinq.features.household.application")) {
                assertTrue(
                        type.equals(HouseholdApplicationService.class),
                        "Controller must not depend on application types other than HouseholdApplicationService"
                );
            }
        }
    }
}
