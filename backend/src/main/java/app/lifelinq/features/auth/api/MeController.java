package app.lifelinq.features.auth.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.contract.UserContextView;
import app.lifelinq.features.auth.contract.UserMembershipView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        return ResponseEntity.ok(toResponse(authApplicationService.getMe(context.getUserId())));
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

    @PutMapping("/me/active-group")
    public ResponseEntity<?> setActiveGroup(@RequestBody SetActiveGroupRequest request) {
        RequestContext context = RequestContextHolder.getCurrent();
        if (context == null || context.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing authenticated context");
        }
        if (request == null || request.getActiveGroupId() == null) {
            return ResponseEntity.badRequest().body("activeGroupId must not be null");
        }
        return ResponseEntity.ok(toResponse(
                authApplicationService.setActiveGroup(context.getUserId(), request.getActiveGroupId())
        ));
    }

    private MeResponse toResponse(UserContextView view) {
        List<MembershipItemResponse> memberships = new ArrayList<>();
        for (UserMembershipView membership : view.memberships()) {
            memberships.add(new MembershipItemResponse(membership.groupId(), membership.role()));
        }
        return new MeResponse(view.userId(), view.activeGroupId(), memberships);
    }

    public static final class SetActiveGroupRequest {
        private UUID activeGroupId;

        public UUID getActiveGroupId() {
            return activeGroupId;
        }

        public void setActiveGroupId(UUID activeGroupId) {
            this.activeGroupId = activeGroupId;
        }
    }

    private static final class MembershipItemResponse {
        private final UUID groupId;
        private final String role;

        private MembershipItemResponse(UUID groupId, String role) {
            this.groupId = groupId;
            this.role = role;
        }

        public UUID getGroupId() {
            return groupId;
        }

        public String getRole() {
            return role;
        }
    }

    private static final class MeResponse {
        private final UUID userId;
        private final UUID activeGroupId;
        private final List<MembershipItemResponse> memberships;

        private MeResponse(UUID userId, UUID activeGroupId, List<MembershipItemResponse> memberships) {
            this.userId = userId;
            this.activeGroupId = activeGroupId;
            this.memberships = memberships;
        }

        public UUID getUserId() {
            return userId;
        }

        public UUID getActiveGroupId() {
            return activeGroupId;
        }

        public List<MembershipItemResponse> getMemberships() {
            return memberships;
        }
    }
}
