package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.TodoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class TodoPersistenceConfig {

    @Bean
    public TodoMapper todoMapper() {
        return new TodoMapper();
    }

    @Bean
    public TodoRepository todoRepository(TodoJpaRepository todoJpaRepository, TodoMapper todoMapper) {
        return new JpaTodoRepositoryAdapter(todoJpaRepository, todoMapper);
    }
}
