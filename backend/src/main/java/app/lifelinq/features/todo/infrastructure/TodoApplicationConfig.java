package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.TodoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoApplicationConfig {

    @Bean
    public TodoApplicationService todoApplicationService(TodoRepository todoRepository) {
        return new TodoApplicationService(todoRepository);
    }
}
