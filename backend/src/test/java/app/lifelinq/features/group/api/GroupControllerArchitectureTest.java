package app.lifelinq.features.group.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.application.GroupApplicationService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GroupControllerArchitectureTest {

    @Test
    void controllerDependsOnlyOnApplicationServiceFromApplicationLayer() {
        Set<Class<?>> referencedTypes = new HashSet<>();
        Class<?> controllerClass = GroupController.class;

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
            if (type.getName().startsWith("app.lifelinq.features.group.application")) {
                assertTrue(
                        type.equals(GroupApplicationService.class),
                        "Controller must not depend on application types other than GroupApplicationService"
                );
            }
        }
    }
}
