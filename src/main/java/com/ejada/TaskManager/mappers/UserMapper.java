package com.ejada.TaskManager.mappers;


import com.ejada.TaskManager.Dto.UserDto;
import com.ejada.TaskManager.entity.Role;
import com.ejada.TaskManager.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.control.MappingControl;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {TaskMapper.class})
public interface UserMapper {

    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    @Mapping(source = "tasks", target = "tasks")
    UserDto toDto(User user);

    @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
    @Mapping(source = "tasks", target = "tasks")
    User toEntity(UserDto userDto);

    @Named("roleToString")
    default String roleToString(Role role) {
        return role != null ? role.getName() : "USER";
    }

    @Named("stringToRole")
    default Role stringToRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

}
