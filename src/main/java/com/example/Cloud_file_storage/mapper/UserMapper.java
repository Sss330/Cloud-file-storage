package com.example.Cloud_file_storage.mapper;

import com.example.Cloud_file_storage.dto.UserDto;
import com.example.Cloud_file_storage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    UserDto toDto(User user);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    User toEntity(UserDto userDto);
}