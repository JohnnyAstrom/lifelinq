package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import app.lifelinq.test.FakeActiveGroupUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class GroupContextFilterTest {

    @AfterEach
    void clearThreadLocals() {
        RequestContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsRequestContextFromPersistedActiveGroupAndClearsAfterChain() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        FakeActiveGroupUserRepository userRepository = new FakeActiveGroupUserRepository().withUser(userId, groupId);
        GroupContextFilter filter = new GroupContextFilter(userRepository);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        FilterChain chain = (req, res) -> {
            RequestContext context = RequestContextHolder.getCurrent();
            assertEquals(userId, context.getUserId());
            assertEquals(groupId, context.getGroupId());
        };

        filter.doFilter(request, response, chain);

        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void proceedsWithNullActiveGroupWhenUserExists() throws Exception {
        UUID userId = UUID.randomUUID();
        FakeActiveGroupUserRepository userRepository = new FakeActiveGroupUserRepository().withUser(userId);
        GroupContextFilter filter = new GroupContextFilter(userRepository);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        FilterChain chain = (req, res) -> {
            RequestContext context = RequestContextHolder.getCurrent();
            assertEquals(userId, context.getUserId());
            assertNull(context.getGroupId());
        };

        filter.doFilter(request, response, chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenAuthenticationMissing() throws Exception {
        GroupContextFilter filter = new GroupContextFilter(new FakeActiveGroupUserRepository());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenPrincipalIsNotUuid() throws Exception {
        GroupContextFilter filter = new GroupContextFilter(new FakeActiveGroupUserRepository());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-a-uuid", null)
        );

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        GroupContextFilter filter = new GroupContextFilter(new FakeActiveGroupUserRepository());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void clearsRequestContextWhenChainThrows() throws Exception {
        UUID userId = UUID.randomUUID();
        FakeActiveGroupUserRepository userRepository = new FakeActiveGroupUserRepository().withUser(userId);
        GroupContextFilter filter = new GroupContextFilter(userRepository);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        FilterChain chain = (req, res) -> {
            throw new ServletException("boom");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
        assertNull(RequestContextHolder.getCurrent());
    }
}
