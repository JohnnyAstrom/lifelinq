package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoApplicationConfig {

    @Bean
    public TodoApplicationService todoApplicationService(
            TodoRepository todoRepository,
            EnsureUserExistsUseCase ensureUserExistsUseCase
    ) {
        return new TodoApplicationService(todoRepository, ensureUserExistsUseCase);
    }
}
