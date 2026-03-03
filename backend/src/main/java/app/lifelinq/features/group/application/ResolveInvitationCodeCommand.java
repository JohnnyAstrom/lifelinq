package app.lifelinq.features.group.application;

public final class ResolveInvitationCodeCommand {
    private final String code;

    public ResolveInvitationCodeCommand(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
