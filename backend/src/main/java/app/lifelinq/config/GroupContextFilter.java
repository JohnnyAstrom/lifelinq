package app.lifelinq.config;

import app.lifelinq.features.user.domain.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public final class GroupContextFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    public GroupContextFilter(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must not be null");
        }
        this.userRepository = userRepository;
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
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            UUID userId;
            try {
                userId = UUID.fromString(String.valueOf(authentication.getPrincipal()));
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            var user = userRepository.findById(userId);
            if (user.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            RequestContext context = new RequestContext();
            context.setUserId(userId);
            context.setGroupId(user.get().getActiveGroupId());
            RequestContextHolder.set(context);
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }
}
