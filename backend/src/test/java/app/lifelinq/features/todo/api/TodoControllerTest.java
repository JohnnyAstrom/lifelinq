package app.lifelinq.features.todo.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.household.application.HouseholdApplicationServiceTestFactory;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TodoControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private TodoApplicationService todoApplicationService;
    private FakeMembershipRepository membershipRepository;

    @BeforeEach
    void setUp() {
        membershipRepository = new FakeMembershipRepository();
        todoApplicationService = Mockito.mock(TodoApplicationService.class);
        TodoController controller = new TodoController(todoApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new RequestContextFilter(
                        new JwtVerifier(SECRET),
                        HouseholdApplicationServiceTestFactory.createForContextResolution(membershipRepository)
                ))
                .build();
    }

    @Test
    void createReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Buy milk\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void createReturns401WhenHouseholdMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(post("/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Buy milk\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void listReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void listReturns401WhenHouseholdMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/todos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void createSucceedsWithValidToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID todoId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(todoApplicationService.createTodo(householdId, userId, "Buy milk", TodoScope.LATER, null, null, null, null, null))
                .thenReturn(todoId);

        mockMvc.perform(post("/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Buy milk\"}"))
                .andExpect(status().isOk());

        verify(todoApplicationService).createTodo(householdId, userId, "Buy milk", TodoScope.LATER, null, null, null, null, null);
    }

    @Test
    void listSucceedsWithValidToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(todoApplicationService.listTodos(householdId, TodoStatus.ALL)).thenReturn(List.of());

        mockMvc.perform(get("/todos")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "ALL"))
                .andExpect(status().isOk());

        verify(todoApplicationService).listTodos(householdId, TodoStatus.ALL);
    }

    @Test
    void calendarReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/todos/calendar/2026/2"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void calendarReturns400ForInvalidMonth() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/todos/calendar/2026/13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void calendarReturns400ForMonthZero() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/todos/calendar/2026/0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void calendarSucceedsWithValidToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(todoApplicationService.listTodosForMonth(householdId, 2026, 2)).thenReturn(List.of());

        mockMvc.perform(get("/todos/calendar/2026/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(todoApplicationService).listTodosForMonth(householdId, 2026, 2);
    }

    @Test
    void updateReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(put("/todos/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Buy milk\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoApplicationService);
    }

    @Test
    void updateSucceedsWithValidToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID todoId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(todoApplicationService.updateTodo(
                todoId,
                userId,
                "Buy oat milk",
                TodoScope.DAY,
                java.time.LocalDate.of(2026, 2, 14),
                java.time.LocalTime.of(9, 30),
                null,
                null,
                null))
                .thenReturn(true);

        mockMvc.perform(put("/todos/" + todoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Buy oat milk\",\"dueDate\":\"2026-02-14\",\"dueTime\":\"09:30:00\"}"))
                .andExpect(status().isOk());

        verify(todoApplicationService).updateTodo(
                todoId,
                userId,
                "Buy oat milk",
                TodoScope.DAY,
                java.time.LocalDate.of(2026, 2, 14),
                java.time.LocalTime.of(9, 30),
                null,
                null,
                null
        );
    }

    private String createToken(UUID userId, Instant exp) throws Exception {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"userId\":\"%s\",\"exp\":%d}",
                userId,
                exp.getEpochSecond()
        );
        String headerPart = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signaturePart = base64Url(hmacSha256(headerPart + "." + payloadPart));
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    private byte[] hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static final class FakeMembershipRepository implements MembershipRepository {
        private final Map<UUID, List<UUID>> byUser = new HashMap<>();

        FakeMembershipRepository withMembership(UUID userId, UUID householdId) {
            byUser.put(userId, List.of(householdId));
            return this;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            return byUser.getOrDefault(userId, List.of());
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
