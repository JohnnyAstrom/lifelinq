package app.lifelinq.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public final class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtVerifier jwtVerifier;

    public AuthenticationFilter(JwtVerifier jwtVerifier) {
        if (jwtVerifier == null) {
            throw new IllegalArgumentException("jwtVerifier must not be null");
        }
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/dev/token".equals(path) || "/auth/dev-login".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
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
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId.toString(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
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
