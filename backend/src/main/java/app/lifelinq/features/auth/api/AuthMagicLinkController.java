package app.lifelinq.features.auth.api;

import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.application.MagicLinkVerificationException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthMagicLinkController {
    private static final String INVALID_LINK_ERROR = "invalid_or_expired";
    private final AuthApplicationService authApplicationService;
    private final String completeBaseUrl;

    public AuthMagicLinkController(
            AuthApplicationService authApplicationService,
            @Value("${lifelinq.auth.magic.completeBaseUrl:mobileapp://auth/complete}") String completeBaseUrl
    ) {
        this.authApplicationService = authApplicationService;
        this.completeBaseUrl = completeBaseUrl;
    }

    @PostMapping("/auth/magic/start")
    public ResponseEntity<Void> start(@RequestBody StartMagicLinkRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        authApplicationService.startMagicLinkLogin(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/magic/verify")
    public ResponseEntity<Void> verify(@RequestParam("token") String token) {
        try {
            String redirectUrl = authApplicationService.verifyMagicLinkAndBuildRedirect(token);
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create(redirectUrl)).build();
        } catch (MagicLinkVerificationException | IllegalArgumentException ex) {
            String failureRedirect = completeBaseUrl + "#error=" + INVALID_LINK_ERROR;
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create(failureRedirect)).build();
        }
    }

    public static final class StartMagicLinkRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

