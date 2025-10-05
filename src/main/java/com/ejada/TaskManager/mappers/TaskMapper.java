package com.ejada.TaskManager.mappers;


import com.ejada.TaskManager.Dto.TaskDto;
import com.ejada.TaskManager.entity.Status;
import com.ejada.TaskManager.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "assignedTo.id", target = "assignedToID")
    @Mapping(source = "assignedTo.username", target = "assignedToUser")
    @Mapping(source = "createdBy.id", target = "createdByID")
    @Mapping(source = "createdBy.username", target = "createdByUser")
//    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "status", target = "status")
//    @Mapping(source = "creationDate", target = "creationDate")
//    @Mapping(source = "dueDate", target = "dueDate")
    TaskDto toDto(Task task);

    @Mapping(target = "assignedTo", ignore = true) // handled in service
    @Mapping(target = "createdBy", ignore = true)  // handled in service
    @Mapping(target = "status", expression = "java(com.ejada.TaskManager.entity.Status.valueOf(taskDto.getStatus()))")
    @Mapping(target = "creationDate", ignore = true) // LocalDateTime mapping
//    @Mapping(source = "dueDate", target = "dueDate")
    Task toEntity(TaskDto taskDto);

//    @Named("statusToString")
//    default String statusToString(Status status) {
//        return status != null ? status.name() : null;
//    }

}
