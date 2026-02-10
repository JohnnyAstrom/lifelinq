package app.lifelinq.features.todo.application;

public final class CompleteTodoResult {
    private final boolean completed;

    public CompleteTodoResult(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }
}
