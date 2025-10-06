package com.ejada.TaskManager.service;


import com.ejada.TaskManager.Dto.TaskDto;
import com.ejada.TaskManager.Dto.UserDto;
import com.ejada.TaskManager.entity.*;
import com.ejada.TaskManager.exception.InvalidOperationException;
import com.ejada.TaskManager.exception.ResourceNotFoundException;
import com.ejada.TaskManager.mappers.TaskMapper;
import com.ejada.TaskManager.mappers.UserMapper;
import com.ejada.TaskManager.repository.RoleRepo;
import com.ejada.TaskManager.repository.TaskRepo;
import com.ejada.TaskManager.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final TaskRepo taskRepo;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final AuthenticationManager manager;
    private final JWTService jwtService;

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto findUserById(int id) {
        return userMapper.toDto(userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    public String login(UserDto userDto) {
        Authentication authentication = manager.authenticate(new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));

        if(!(authentication.isAuthenticated()))
            throw new InvalidOperationException("Invalid credentials");


        return jwtService.generateToken(userDto.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto createUser(UserDto userDto) {
        if (userRepo.existsByEmail(userDto.getEmail())) {
            throw new InvalidOperationException("This user already exists!");
        }

        User user = userMapper.toEntity(userDto);

        String roleName = (userDto.getRole() != null) ? userDto.getRole() : "USER";
        Role role = roleRepo.findByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.setRole(role);

        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }

        return userMapper.toDto(userRepo.save(user));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public TaskDto findTaskById(int id, MyUserDetails currentUser) {
        TaskDto taskDto;
        Task task = taskRepo.findTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if(isAdmin) {
            taskDto = taskMapper.toDto(task);
        } else {
            int userID = currentUser.getId();
            if(task.getAssignedTo().getId() == userID) {
                taskDto = taskMapper.toDto(task);
            } else {
                throw new InvalidOperationException("Unauthorized user to get this task!");
            }
        }

        return taskDto;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<TaskDto> findTasks(Status status, Integer assignee, MyUserDetails currentUser) {
        List<Task> tasks = new ArrayList<>();
        List<TaskDto> taskDtos = new ArrayList<>();

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if(isAdmin) {
            if (status == null && assignee == null) {
                tasks = taskRepo.findAll();
            } else {
                tasks = taskRepo.findByStatusOrAssignee(status, assignee);
            }
        } else {
            int userID = currentUser.getId();
            if (status == null) {
                tasks = taskRepo.findByStatusOrAssignee(status, userID);
            } else {
                tasks = taskRepo.findByStatusOrAssignee(status, userID);
            }
        }

        for(Task task:tasks) {
            TaskDto taskDto = taskMapper.toDto(task);
            taskDtos.add(taskDto);
        }

        return taskDtos;
    }

    public TaskDto createTask(TaskDto taskDto) {
        taskDto.setStatus((taskDto.getStatus() != null) ? taskDto.getStatus() : "OPEN");
        if(taskDto.getDueDate() == null) {
            throw new ResourceNotFoundException("dueDate is required");
        }

        Task task = taskMapper.toEntity(taskDto);

        User creator = userRepo.findById(taskDto.getCreatedByID()).orElseThrow(() -> new ResourceNotFoundException("Not found creator id "+taskDto.getCreatedByID()));

        int assigneeId = (taskDto.getAssignedToID() != 0) ? taskDto.getAssignedToID() : taskDto.getCreatedByID();
        User assignee = userRepo.findById(assigneeId).orElseThrow(() -> new ResourceNotFoundException("Not found Assignee id "+taskDto.getAssignedToID()));

        if(creator.getRole().getName().equals("USER")) {
            if(creator.getId() != assignee.getId()) {
                throw new InvalidOperationException("Normal users can only assign tasks to themselves");
            }
        }

        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        task.setCreationDate(LocalDateTime.now());
        if(task.getDueDate().isBefore(task.getCreationDate().toLocalDate())) {
            throw new InvalidOperationException("Due date can't be earlier than creation date!");
        }
        return taskMapper.toDto(taskRepo.save(task));
    }

//    public TaskDto updateTask(int id, TaskDto updatedTask) {
//        Task task = taskRepo.findTaskById(id).orElseThrow(() -> new RuntimeException("Task not found with id "+id));
//
//        task.setTitle(updatedTask.getTitle());
//        task.setDescription(updatedTask.getDescription());
//        task.setStatus(Status.valueOf(updatedTask.getStatus()));
//
//        return taskMapper.toDto(taskRepo.save(task));
//    }

    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public TaskDto updateTaskQuery(int id, TaskDto updatedTask) {
        int assigneeId = updatedTask.getAssignedToID();
        if (assigneeId == 0) {
            throw new InvalidOperationException("assignedToID is required");
        }

        Task oldTask = taskRepo.findTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id: "+id));
        User creator = oldTask.getCreatedBy();
        if(creator.getRole().getName().equals("USER")) {
            if(oldTask.getAssignedTo().getId() != assigneeId)
                throw new InvalidOperationException("You aren't allowed to assign tasks to another user");
        }

        int updates = taskRepo.updateTaskQuery(
                id, updatedTask.getTitle(), updatedTask.getDescription(),
                assigneeId
        );

        if(updates == 0) {
            throw new InvalidOperationException("Failed Updating Task id "+id+"!");
        }
        Task task = taskRepo.findTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task lost after updating"));

        return taskMapper.toDto(task);
    }

    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public TaskDto updateStatus(int id, String updatedStatus) {
        Task task = taskRepo.findTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id "+id));

        task.setStatus(Status.valueOf(updatedStatus));

        return taskMapper.toDto(taskRepo.save(task));
    }

    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public TaskDto updateDueDate(int id, LocalDate newDueDate) {
        Task task = taskRepo.findTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id: "+id));

        if(newDueDate.isBefore(task.getCreationDate().toLocalDate())) {
            throw new InvalidOperationException("Due date can't be earlier than creation date!");
        }

        taskRepo.updateDueDate(id, newDueDate);

        Task newTask = taskRepo.findTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task lost after updating"));
        return taskMapper.toDto(newTask);
    }

    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isOwner(#id, authentication)")
    public void deleteTask(int id) {
        taskRepo.deleteTaskById(id);
    }

}
