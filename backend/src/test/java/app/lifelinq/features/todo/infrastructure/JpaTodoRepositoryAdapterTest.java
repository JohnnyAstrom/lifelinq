package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TodoJpaTestApplication.class)
@ActiveProfiles("test")
class JpaTodoRepositoryAdapterTest {

    @Autowired
    private TodoJpaRepository todoJpaRepository;

    @Test
    void savesAndLoadsRoundTrip() {
        JpaTodoRepositoryAdapter adapter = new JpaTodoRepositoryAdapter(todoJpaRepository, new TodoMapper());
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Write tests");

        adapter.save(todo);
        Optional<Todo> loaded = adapter.findById(todo.getId());

        assertTrue(loaded.isPresent());
        assertEquals(todo.getId(), loaded.get().getId());
        assertEquals(todo.getHouseholdId(), loaded.get().getHouseholdId());
        assertEquals(todo.getText(), loaded.get().getText());
        assertEquals(TodoStatus.OPEN, loaded.get().getStatus());

        loaded.get().toggle(Instant.parse("2026-01-01T00:00:00Z"));
        assertEquals(TodoStatus.COMPLETED, loaded.get().getStatus());
    }
}
