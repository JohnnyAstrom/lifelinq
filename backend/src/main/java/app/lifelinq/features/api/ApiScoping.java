package app.lifelinq.features.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public final class ApiScoping {
    private ApiScoping() {
    }

    public static RequestContext getContext() {
        return RequestContextHolder.getCurrent();
    }

    public static ResponseEntity<String> missingContext() {
        return ResponseEntity.status(401).body("Missing household context");
    }

    public static boolean matchesHousehold(RequestContext context, UUID householdId) {
        return context != null
                && context.getHouseholdId() != null
                && context.getHouseholdId().equals(householdId);
    }

    public static ResponseEntity<String> householdMismatch() {
        return ResponseEntity.status(403).body("Household mismatch");
    }
}
