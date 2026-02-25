package app.lifelinq.features.auth.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import app.lifelinq.features.auth.application.AuthApplicationService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
    private final AuthApplicationService authApplicationService;

    public MeController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        RequestContext context = RequestContextHolder.getCurrent();
        if (context == null || context.getUserId() == null) {
            return ResponseEntity.status(401).body("Missing authenticated context");
        }
        return ResponseEntity.ok(new MeResponse(context.getUserId(), context.getGroupId()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe() {
        RequestContext context = RequestContextHolder.getCurrent();
        if (context == null || context.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing authenticated context");
        }
        authApplicationService.deleteAccount(context.getUserId());
        return ResponseEntity.noContent().build();
    }

    private static final class MeResponse {
        private final UUID userId;
        private final UUID groupId;

        private MeResponse(UUID userId, UUID groupId) {
            this.userId = userId;
            this.groupId = groupId;
        }

        public UUID getUserId() {
            return userId;
        }

        public UUID getGroupId() {
            return groupId;
        }
    }
}
