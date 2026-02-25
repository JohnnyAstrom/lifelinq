package app.lifelinq.config;

public final class NoActiveGroupSelectedException extends RuntimeException {
    public NoActiveGroupSelectedException() {
        super("No active group selected");
    }
}
