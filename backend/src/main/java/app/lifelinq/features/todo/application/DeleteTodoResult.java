package app.lifelinq.features.todo.application;

public final class DeleteTodoResult {
    private final boolean deleted;

    public DeleteTodoResult(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
