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

    @Column(name = "active_group_id")
    private UUID activeGroupId;

    protected UserEntity() {
    }

    public UserEntity(UUID id) {
        this(id, null);
    }

    public UserEntity(UUID id, UUID activeGroupId) {
        this.id = id;
        this.activeGroupId = activeGroupId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActiveGroupId() {
        return activeGroupId;
    }
}
