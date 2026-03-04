package app.lifelinq.features.user.domain;

import java.util.UUID;

public final class User {
    private final UUID id;
    private final UUID activeGroupId;
    private final String email;
    private final String firstName;
    private final String lastName;

    public User(UUID id) {
        this(id, null, null, null, null);
    }

    public User(UUID id, UUID activeGroupId) {
        this(id, activeGroupId, null, null, null);
    }

    public User(UUID id, UUID activeGroupId, String firstName, String lastName) {
        this(id, activeGroupId, null, firstName, lastName);
    }

    public User(UUID id, UUID activeGroupId, String email, String firstName, String lastName) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.activeGroupId = activeGroupId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActiveGroupId() {
        return activeGroupId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public User withActiveGroupId(UUID activeGroupId) {
        return new User(id, activeGroupId, email, firstName, lastName);
    }

    public User withProfile(String firstName, String lastName) {
        return new User(id, activeGroupId, email, firstName, lastName);
    }

    public User withEmail(String email) {
        return new User(id, activeGroupId, email, firstName, lastName);
    }
}
