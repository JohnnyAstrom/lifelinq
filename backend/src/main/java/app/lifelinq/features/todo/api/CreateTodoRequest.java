package app.lifelinq.features.todo.api;

public final class CreateTodoRequest {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
