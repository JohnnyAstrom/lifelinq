package app.lifelinq.features.auth.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        RequestContext context = RequestContextHolder.getCurrent();
        if (context == null || context.getUserId() == null) {
            return ResponseEntity.status(401).body("Missing authenticated context");
        }
        return ResponseEntity.ok(new MeResponse(context.getUserId(), context.getHouseholdId()));
    }

    private static final class MeResponse {
        private final UUID userId;
        private final UUID householdId;

        private MeResponse(UUID userId, UUID householdId) {
            this.userId = userId;
            this.householdId = householdId;
        }

        public UUID getUserId() {
            return userId;
        }

        public UUID getHouseholdId() {
            return householdId;
        }
    }
}
