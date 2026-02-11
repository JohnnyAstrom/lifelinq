package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.CompleteTodoUseCase;
import app.lifelinq.features.todo.application.CreateTodoUseCase;
import app.lifelinq.features.todo.application.ListTodosUseCase;
import app.lifelinq.features.todo.domain.TodoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoApplicationConfig {

    @Bean
    public CreateTodoUseCase createTodoUseCase() {
        return new CreateTodoUseCase();
    }

    @Bean
    public CompleteTodoUseCase completeTodoUseCase(TodoRepository todoRepository) {
        return new CompleteTodoUseCase(todoRepository);
    }

    @Bean
    public ListTodosUseCase listTodosUseCase(TodoRepository todoRepository) {
        return new ListTodosUseCase(todoRepository);
    }
}
