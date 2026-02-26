package app.lifelinq.features.user.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import app.lifelinq.features.user.application.UserApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class UserController {
    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        RequestContext context = RequestContextHolder.getCurrent();
        if (context == null || context.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing authenticated context");
        }
        if (request == null) {
            return ResponseEntity.badRequest().body("Request body must not be null");
        }

        userApplicationService.updateProfile(
                context.getUserId(),
                request.getFirstName(),
                request.getLastName()
        );
        return ResponseEntity.noContent().build();
    }

    public static final class UpdateProfileRequest {
        private String firstName;
        private String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
