package app.lifelinq.features.todo.application;

import java.util.UUID;

final class TodoMonthQuery {
    private final UUID householdId;
    private final int year;
    private final int month;

    TodoMonthQuery(UUID householdId, int year, int month) {
        this.householdId = householdId;
        this.year = year;
        this.month = month;
    }

    UUID getHouseholdId() {
        return householdId;
    }

    int getYear() {
        return year;
    }

    int getMonth() {
        return month;
    }
}
