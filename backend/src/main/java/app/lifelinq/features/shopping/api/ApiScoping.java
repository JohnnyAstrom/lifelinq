package app.lifelinq.features.shopping.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import org.springframework.http.ResponseEntity;

final class ApiScoping {
    private ApiScoping() {
    }

    static RequestContext getContext() {
        return RequestContextHolder.getCurrent();
    }

    static ResponseEntity<String> missingContext() {
        return ResponseEntity.status(401).body("Missing group context");
    }
}
