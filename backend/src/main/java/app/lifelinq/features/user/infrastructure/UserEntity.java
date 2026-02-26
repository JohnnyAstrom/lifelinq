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

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    protected UserEntity() {
    }

    public UserEntity(UUID id) {
        this(id, null, null, null);
    }

    public UserEntity(UUID id, UUID activeGroupId) {
        this(id, activeGroupId, null, null);
    }

    public UserEntity(UUID id, UUID activeGroupId, String firstName, String lastName) {
        this.id = id;
        this.activeGroupId = activeGroupId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActiveGroupId() {
        return activeGroupId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
