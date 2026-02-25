package app.lifelinq.features.documents.api;

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
        RequestContext context = getContext();
        if (context != null && context.getUserId() != null && context.getGroupId() == null) {
            throw new app.lifelinq.config.NoActiveGroupSelectedException();
        }
        return ResponseEntity.status(401).body("Missing group context");
    }
}
