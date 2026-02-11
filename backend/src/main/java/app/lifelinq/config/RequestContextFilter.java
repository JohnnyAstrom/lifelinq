package app.lifelinq.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RequestContext context = new RequestContext();
        // TODO: Temporary header-based context; replace with JWT-derived scoping.
        populateFromHeaders(request, context);
        RequestContextHolder.set(context);
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

    private void populateFromHeaders(HttpServletRequest request, RequestContext context) {
        context.setHouseholdId(parseUuidHeader(request, "X-Household-Id"));
        context.setUserId(parseUuidHeader(request, "X-User-Id"));
    }

    private UUID parseUuidHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
