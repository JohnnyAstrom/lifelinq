package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Group;

public final class GroupMapper {

    public GroupEntity toEntity(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        return new GroupEntity(group.getId(), group.getName());
    }

    public Group toDomain(GroupEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        return new Group(entity.getId(), entity.getName());
    }
}
