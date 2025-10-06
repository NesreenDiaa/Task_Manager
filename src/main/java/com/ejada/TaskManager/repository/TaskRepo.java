package com.ejada.TaskManager.repository;

import com.ejada.TaskManager.Dto.TaskDto;
import com.ejada.TaskManager.entity.Status;
import com.ejada.TaskManager.entity.Task;
import com.ejada.TaskManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepo extends JpaRepository<Task, Integer> {

    Optional<Task> findTaskById(int theId);

    @Query("SELECT t FROM Task t"+
            " WHERE (:status IS NULL OR t.status = :status)"+
            " AND (:assigneeId IS NULL OR t.assignedTo.id = :assigneeId)"
    )
    List<Task> findByStatusOrAssignee(
            @Param("status") Status status,
            @Param("assigneeId") Integer assignee
    );

//    @Query("SELECT t FROM Task t"+
//            " WHERE (:status IS NULL OR t.status = :status)"+
//            " AND (:assigneeId IS NULL OR t.assignedTo.id = :assigneeId)"
//    )
//    List<Task> findByStatusOrAssignee(
//            @Param("status") Status status,
//            @Param("assigneeId") Integer assignee
//    );

//    @Query("SELECT t FROM Task t"+
//            " WHERE t.assignedTo.id = :assigneeId"
//    )
//    List<Task> findByAssigneeId(@Param("assigneeId") int userId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.title = :title, t.description = :description, t.assignedTo.id = :assigneeId WHERE t.id = :id")
    int updateTaskQuery(@Param("id") int theId, @Param("title") String title,
                        @Param("description") String description, @Param("assigneeId") int assigneeId);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.dueDate = :newDate WHERE t.id = :id")
    void updateDueDate(@Param("id") int theId, @Param("newDate") LocalDate newDueDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.id = :id")
    void deleteTaskById(@Param("id") int theId);
}
