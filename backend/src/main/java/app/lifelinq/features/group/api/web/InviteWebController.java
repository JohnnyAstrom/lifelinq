package app.lifelinq.features.group.api.web;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.application.PreviewInvitationReason;
import app.lifelinq.features.group.application.PreviewInvitationResult;
import app.lifelinq.features.group.domain.InvitationType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class InviteWebController {
    private final GroupApplicationService groupApplicationService;

    public InviteWebController(GroupApplicationService groupApplicationService) {
        this.groupApplicationService = groupApplicationService;
    }

    @GetMapping(value = "/invite/{token}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> previewInvitation(@PathVariable String token) {
        PreviewInvitationResult preview = groupApplicationService.previewInvitation(token);
        return ResponseEntity.ok(renderHtml(preview));
    }

    private String renderHtml(PreviewInvitationResult preview) {
        InvitationCopy copy = resolveCopy(preview);
        String escapedShortCode = preview.getShortCode() == null ? "" : escapeHtml(preview.getShortCode());
        String validExtras = preview.isValid()
                ? """
                      <div class="button">Open in the app</div>
                      <div class="code-label">Invite code</div>
                      <div class="code-box">%s</div>
                      <button class="copy-button" onclick="navigator.clipboard.writeText('%s')">Copy</button>
                      """.formatted(escapedShortCode, escapedShortCode)
                : "";
        String placeBlock = preview.isValid()
                ? """
                      <div class="place">%s</div>
                      """.formatted(escapeHtml(copy.placeName()))
                : "";
        String footer = preview.isValid()
                ? """
                      <div class="footer">
                        LifeLinq helps you coordinate life together.
                      </div>
                      """
                : "";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>LifeLinq Invitation</title>
                <style>
                  body {
                    margin: 0;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    background: #f5f7fa;
                    display: flex;
                    justify-content: center;
                    padding: 80px 20px 40px;
                  }

                  .container {
                    max-width: 440px;
                    width: 100%%;
                    background: #ffffff;
                    border-radius: 14px;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.08);
                    padding: 36px 28px;
                    text-align: center;
                  }

                  .logo {
                    font-weight: 600;
                    font-size: 18px;
                    margin-bottom: 24px;
                    color: #222;
                  }

                  h1 {
                    font-size: 22px;
                    font-weight: 600;
                    margin: 0 0 6px;
                  }

                  .place {
                    font-size: 26px;
                    font-weight: 700;
                    margin-bottom: 18px;
                    color: #111;
                  }

                  p {
                    font-size: 15px;
                    color: #555;
                    margin: 0 0 20px;
                  }

                  .button {
                    display: block;
                    width: 100%%;
                    padding: 14px;
                    border-radius: 8px;
                    background: #111;
                    color: white;
                    text-decoration: none;
                    font-weight: 600;
                    margin-bottom: 16px;
                    opacity: 0.5;
                    cursor: default;
                  }

                  .code-label {
                    font-size: 13px;
                    color: #888;
                    margin-bottom: 6px;
                  }

                  .code-box {
                    font-family: monospace;
                    font-size: 16px;
                    background: #f1f3f5;
                    padding: 12px;
                    border-radius: 8px;
                    word-break: break-all;
                    margin-bottom: 18px;
                  }

                  .footer {
                    font-size: 13px;
                    color: #999;
                    margin-top: 24px;
                  }

                  .copy-button {
                    border: 1px solid #d0d5dd;
                    background: #ffffff;
                    color: #333;
                    border-radius: 8px;
                    padding: 8px 14px;
                    font-size: 14px;
                    cursor: pointer;
                  }
                </style>
                </head>
                <body>
                  <div class="container">
                    <div class="logo">LifeLinq</div>

                    <h1>%s</h1>
                    %s
                    <p>%s</p>

                    %s

                    %s
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(copy.inviterLine()),
                placeBlock,
                escapeHtml(copy.subText()),
                validExtras,
                footer
        );
    }

    private InvitationCopy resolveCopy(PreviewInvitationResult preview) {
        String placeName = preview.getPlaceName() == null || preview.getPlaceName().isBlank()
                ? "this place"
                : preview.getPlaceName();
        if (preview.isValid()) {
            if (preview.getType() == InvitationType.EMAIL
                    && preview.getInviterDisplayName() != null
                    && !preview.getInviterDisplayName().isBlank()) {
                return new InvitationCopy(
                        preview.getInviterDisplayName() + " invited you to join",
                        placeName,
                        "Open the LifeLinq app to continue."
                );
            }
            return new InvitationCopy(
                    "You're invited to join",
                    placeName,
                    "This is a shared invitation link."
            );
        }
        PreviewInvitationReason reason = preview.getReason();
        if (reason == PreviewInvitationReason.EXPIRED) {
            return new InvitationCopy(
                    "This invitation has expired",
                    "",
                    "Ask the person who invited you to send a new invitation."
            );
        }
        if (reason == PreviewInvitationReason.REVOKED) {
            return new InvitationCopy(
                    "This invitation is no longer valid",
                    "",
                    "Ask the person who invited you to send a new invitation."
            );
        }
        if (reason == PreviewInvitationReason.EXHAUSTED) {
            return new InvitationCopy(
                    "This invitation has already been used",
                    "",
                    "If you still need access, ask the person who invited you to send a new invitation."
            );
        }
        return new InvitationCopy(
                "Invitation not found",
                "",
                "This link may be incorrect or too old. Ask the person who invited you to send a new invitation."
        );
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record InvitationCopy(String inviterLine, String placeName, String subText) {
    }
}
