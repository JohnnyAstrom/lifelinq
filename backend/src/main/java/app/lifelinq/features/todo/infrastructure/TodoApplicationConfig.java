package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.user.application.UserApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoApplicationConfig {

    @Bean
    public TodoApplicationService todoApplicationService(
            TodoRepository todoRepository,
            UserApplicationService userApplicationService
    ) {
        return new TodoApplicationService(todoRepository, userApplicationService);
    }
}
