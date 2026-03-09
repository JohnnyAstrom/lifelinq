package app.lifelinq.features.economy.api;

import app.lifelinq.config.ApiErrorResponse;
import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import org.springframework.http.ResponseEntity;

final class ApiScoping {
    private ApiScoping() {
    }

    static RequestContext getContext() {
        return RequestContextHolder.getCurrent();
    }

    static ResponseEntity<ApiErrorResponse> missingContext() {
        RequestContext context = getContext();
        if (context != null && context.getUserId() != null && context.getGroupId() == null) {
            throw new app.lifelinq.config.NoActiveGroupSelectedException();
        }
        return ResponseEntity.status(401).body(new ApiErrorResponse("MISSING_GROUP_CONTEXT", "Missing group context"));
    }
}
