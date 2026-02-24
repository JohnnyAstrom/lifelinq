package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoJpaRepository extends JpaRepository<TodoEntity, UUID> {
    @Query("""
            select t
            from TodoEntity t
            where t.deletedAt is null
              and t.householdId = :householdId
              and (:status is null or t.status = :status)
            order by
              case
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.DAY then 0
                when t.scope is null and t.dueDate is not null then 0
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.WEEK then 1
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.MONTH then 2
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.LATER then 3
                else 4
              end asc,
              case
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.DAY
                  or (t.scope is null and t.dueDate is not null) then t.dueDate
                else null
              end asc,
              case
                when (t.scope = app.lifelinq.features.todo.domain.TodoScope.DAY
                      or (t.scope is null and t.dueDate is not null))
                     and t.dueTime is null then 1
                else 0
              end asc,
              case
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.DAY
                  or (t.scope is null and t.dueDate is not null) then t.dueTime
                else null
              end asc,
              case
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.WEEK
                  or t.scope = app.lifelinq.features.todo.domain.TodoScope.MONTH
                  or t.scope = app.lifelinq.features.todo.domain.TodoScope.LATER
                  or (t.scope is null and t.dueDate is null)
                  then t.createdAt
                else null
              end asc,
              t.id asc
            """)
    List<TodoEntity> listForHousehold(
            @Param("householdId") UUID householdId,
            @Param("status") TodoStatus status
    );

    @Query("""
            select t
            from TodoEntity t
            where t.deletedAt is null
              and t.householdId = :householdId
              and (
                ((t.scope = app.lifelinq.features.todo.domain.TodoScope.DAY or (t.scope is null and t.dueDate is not null))
                  and t.dueDate between :startDate and :endDate)
                or
                (t.scope = app.lifelinq.features.todo.domain.TodoScope.MONTH and t.scopeYear = :year and t.scopeMonth = :month)
              )
            order by
              case
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.DAY then 0
                when t.scope is null and t.dueDate is not null then 0
                when t.scope = app.lifelinq.features.todo.domain.TodoScope.MONTH then 1
                else 9
              end asc,
              case when t.dueDate between :startDate and :endDate then t.dueDate else null end asc,
              case when t.dueDate between :startDate and :endDate and t.dueTime is null then 1 else 0 end asc,
              case when t.dueDate between :startDate and :endDate then t.dueTime else null end asc,
              case when t.scope = app.lifelinq.features.todo.domain.TodoScope.MONTH then t.createdAt else null end asc,
              t.id asc
            """)
    List<TodoEntity> listForMonth(
            @Param("householdId") UUID householdId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
