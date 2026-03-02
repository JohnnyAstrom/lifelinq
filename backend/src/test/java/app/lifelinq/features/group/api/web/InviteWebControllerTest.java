package app.lifelinq.features.group.api.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.application.PreviewInvitationReason;
import app.lifelinq.features.group.application.PreviewInvitationResult;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InviteWebControllerTest {
    private MockMvc mockMvc;
    private GroupApplicationService groupApplicationService;

    @BeforeEach
    void setUp() {
        groupApplicationService = Mockito.mock(GroupApplicationService.class);
        InviteWebController controller = new InviteWebController(groupApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void rendersPersonalizedCopyForValidEmailInviteWithInviterName() throws Exception {
        when(groupApplicationService.previewInvitation("invite-token"))
                .thenReturn(new PreviewInvitationResult(
                        true,
                        PreviewInvitationReason.VALID,
                        "Family",
                        "Alex Doe",
                        Instant.parse("2026-01-10T00:00:00Z"),
                        InvitationType.EMAIL
                ));

        mockMvc.perform(get("/invite/invite-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Alex Doe invited you to join")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Family")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Open the LifeLinq app to continue.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invite code")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("invite-token")));
    }

    @Test
    void returns200AndRendersGenericCopyForValidLinkInvite() throws Exception {
        when(groupApplicationService.previewInvitation("invite-token"))
                .thenReturn(new PreviewInvitationResult(
                        true,
                        PreviewInvitationReason.VALID,
                        "Family",
                        null,
                        Instant.parse("2026-01-10T00:00:00Z"),
                        InvitationType.LINK
                ));

        mockMvc.perform(get("/invite/invite-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("You&#39;re invited to join")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Family")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This is a shared invitation link.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invite code")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("invite-token")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Open in the app")));
    }

    @Test
    void rendersGenericFallbackWhenEmailInviteHasNoInviterName() throws Exception {
        when(groupApplicationService.previewInvitation("invite-token"))
                .thenReturn(new PreviewInvitationResult(
                        true,
                        PreviewInvitationReason.VALID,
                        "Family",
                        null,
                        Instant.parse("2026-01-10T00:00:00Z"),
                        InvitationType.EMAIL
                ));

        mockMvc.perform(get("/invite/invite-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("You&#39;re invited to join")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Family")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Alex Doe invited you to join"))));
    }

    @Test
    void rendersInvalidCopyForMissingInvite() throws Exception {
        when(groupApplicationService.previewInvitation("missing-token"))
                .thenReturn(new PreviewInvitationResult(
                        false,
                        PreviewInvitationReason.NOT_FOUND,
                        null,
                        null,
                        null,
                        null
                ));

        mockMvc.perform(get("/invite/missing-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invitation not found")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This link may be incorrect or too old. Ask the person who invited you to send a new invitation.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Open in the app"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Invite code"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("missing-token"))));
    }
}
