package app.lifelinq.features.auth.api;

import app.lifelinq.test.DevAuthTestApplication;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DevAuthTestApplication.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "lifelinq.devAuth.enabled=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class DevTokenControllerEnabledTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void devTokenReturnsTokenWhenEnabled() throws Exception {
        mockMvc.perform(post("/dev/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    if (!body.contains("\"token\"")) {
                        throw new AssertionError("Expected token in response body");
                    }
                });
    }
}
