package app.lifelinq.features.todo.application;

public final class UpdateTodoResult {
    private final boolean updated;

    public UpdateTodoResult(boolean updated) {
        this.updated = updated;
    }

    public boolean isUpdated() {
        return updated;
    }
}
