package app.lifelinq.features.shopping.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ShoppingControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private ShoppingApplicationService shoppingApplicationService;

    @BeforeEach
    void setUp() {
        shoppingApplicationService = Mockito.mock(ShoppingApplicationService.class);
        ShoppingController controller = new ShoppingController(shoppingApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new RequestContextFilter(new JwtVerifier(SECRET)))
                .build();
    }

    @Test
    void createReturns401WhenTokenMissing() throws Exception {
        mockMvc.perform(post("/shopping-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shoppingApplicationService);
    }

    @Test
    void createReturns401WhenTokenInvalid() throws Exception {
        mockMvc.perform(post("/shopping-items")
                        .header("Authorization", "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shoppingApplicationService);
    }

    @Test
    void createSucceedsWithValidToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        String token = createToken(householdId, userId, Instant.now().plusSeconds(60));

        when(shoppingApplicationService.addItem(householdId, "Milk")).thenReturn(itemId);

        mockMvc.perform(post("/shopping-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Milk\"}"))
                .andExpect(status().isOk());

        verify(shoppingApplicationService).addItem(householdId, "Milk");
    }

    private String createToken(UUID householdId, UUID userId, Instant exp) throws Exception {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"householdId\":\"%s\",\"userId\":\"%s\",\"exp\":%d}",
                householdId,
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
}
