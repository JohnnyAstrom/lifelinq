package app.lifelinq.features.todo.application;

import java.util.UUID;

final class TodoMonthQuery {
    private final UUID groupId;
    private final int year;
    private final int month;

    TodoMonthQuery(UUID groupId, int year, int month) {
        this.groupId = groupId;
        this.year = year;
        this.month = month;
    }

    UUID getGroupId() {
        return groupId;
    }

    int getYear() {
        return year;
    }

    int getMonth() {
        return month;
    }
}
