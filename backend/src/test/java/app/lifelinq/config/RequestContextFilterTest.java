package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequestContextFilterTest {

    @Test
    void setsContextFromHeadersAndClearsAfterChain() throws Exception {
        RequestContextFilter filter = new RequestContextFilter();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Mockito.when(request.getHeader("X-Household-Id")).thenReturn(householdId.toString());
        Mockito.when(request.getHeader("X-User-Id")).thenReturn(userId.toString());

        FilterChain chain = (req, res) -> {
            RequestContext context = RequestContextHolder.getCurrent();
            assertEquals(householdId, context.getHouseholdId());
            assertEquals(userId, context.getUserId());
        };

        filter.doFilter(request, response, chain);

        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void clearsContextWhenChainThrows() throws Exception {
        RequestContextFilter filter = new RequestContextFilter();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Mockito.when(request.getHeader("X-Household-Id")).thenReturn(UUID.randomUUID().toString());

        FilterChain chain = (req, res) -> {
            throw new ServletException("boom");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));

        assertNull(RequestContextHolder.getCurrent());
    }
}
