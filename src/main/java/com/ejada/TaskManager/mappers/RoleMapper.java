package com.ejada.TaskManager.mappers;


import com.ejada.TaskManager.Dto.RoleDto;
import com.ejada.TaskManager.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface RoleMapper {

    RoleDto toDto(Role role);
    Role toEntity(RoleDto roleDto);
}
