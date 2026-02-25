package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.user.contract.UserProvisioning;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoApplicationConfig {

    @Bean
    public TodoApplicationService todoApplicationService(
            TodoRepository todoRepository,
            UserProvisioning userProvisioning
    ) {
        return new TodoApplicationService(todoRepository, userProvisioning);
    }
}
