package app.lifelinq.features.todo.api;

public final class CompleteTodoResponse {
    private final boolean completed;

    public CompleteTodoResponse(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }
}
