package com.ejada.TaskManager.security;

import com.ejada.TaskManager.entity.Task;
import com.ejada.TaskManager.repository.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("taskSecurity")
public class TaskSecurity {

    @Autowired
    private TaskRepo taskRepo;

    public boolean isOwner(int taskId, Authentication authentication) {
        Task task = taskRepo.findTaskById(taskId)
                .orElse(null);

        if (task == null) return false;

        String username = authentication.getName();
        return task.getCreatedBy().getUsername().equals(username);
    }
}
