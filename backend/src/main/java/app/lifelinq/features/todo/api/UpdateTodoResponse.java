package app.lifelinq.features.todo.api;

public final class UpdateTodoResponse {
    private final boolean updated;

    public UpdateTodoResponse(boolean updated) {
        this.updated = updated;
    }

    public boolean isUpdated() {
        return updated;
    }
}
