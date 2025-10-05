package com.ejada.TaskManager.controller;

import com.ejada.TaskManager.Dto.TaskDto;
import com.ejada.TaskManager.Dto.UserDto;
import com.ejada.TaskManager.entity.Status;
import com.ejada.TaskManager.service.SystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyRestController {

    private final SystemService systemService;

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> userLogin(@Valid @RequestBody UserDto userDto ) {
        String token = systemService.login(userDto);
        return ResponseEntity.ok(Map.of("jwt", token));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(systemService.createUser(userDto));
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        return ResponseEntity.ok(systemService.createTask(taskDto));
    }


    // GET /api/users/{id} - Get user
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable int id) {
        return ResponseEntity.ok(systemService.findUserById(id));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable int id) {
        return ResponseEntity.ok(systemService.findTaskById(id));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDto>> getAllTasks(@RequestParam(name = "status", required = false) Status status,
                                                     @RequestParam(name = "assigneeID", required = false) Integer assigneeId) {
        return ResponseEntity.ok(systemService.findTasks(status, assigneeId));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDto> updateTaskQuery(@PathVariable int id,
                                                   @Valid @RequestBody TaskDto updatedTask) {
        return ResponseEntity.ok(systemService.updateTaskQuery(id, updatedTask));
    }

    @PatchMapping("/tasks/{id}/new_due_date")
    public ResponseEntity<TaskDto> updateDueDate(@PathVariable int id,
                                                 @RequestParam(value = "Due_Date", required = true) LocalDate newDate) {
        return ResponseEntity.ok(systemService.updateDueDate(id, newDate));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable int id) {
        systemService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskDto> updateStatus(@PathVariable int id,
                                                @RequestParam(value = "Updated_Status", required = true) String status) {
        return ResponseEntity.ok(systemService.updateStatus(id, status));
    }
}
