package app.lifelinq.features.group.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "households")
public class GroupEntity {
    @Id
    private UUID id;

    @Column
    private String name;

    protected GroupEntity() {
    }

    public GroupEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
