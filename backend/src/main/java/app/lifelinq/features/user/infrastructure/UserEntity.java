package app.lifelinq.features.user.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    protected UserEntity() {
    }

    public UserEntity(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
