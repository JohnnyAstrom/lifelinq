package app.lifelinq.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.filter.OncePerRequestFilter;
import app.lifelinq.features.household.application.ResolveHouseholdForUserUseCase;

public class RequestContextFilter extends OncePerRequestFilter {
    private final JwtVerifier jwtVerifier;
    private final ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase;

    public RequestContextFilter(JwtVerifier jwtVerifier, ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase) {
        this.jwtVerifier = jwtVerifier;
        this.resolveHouseholdForUserUseCase = resolveHouseholdForUserUseCase;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = extractBearerToken(request);
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            JwtClaims claims;
            try {
                claims = jwtVerifier.verify(token);
            } catch (JwtValidationException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UUID userId = claims.getUserId();
            Optional<UUID> householdId;
            try {
                householdId = resolveHouseholdForUserUseCase.resolveForUser(userId);
            } catch (app.lifelinq.features.household.application.AmbiguousHouseholdException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            RequestContext context = new RequestContext();
            context.setHouseholdId(householdId.orElse(null));
            context.setUserId(userId);
            RequestContextHolder.set(context);
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            return null;
        }
        String prefix = "Bearer ";
        if (!header.startsWith(prefix) || header.length() <= prefix.length()) {
            return null;
        }
        return header.substring(prefix.length()).trim();
    }
}
