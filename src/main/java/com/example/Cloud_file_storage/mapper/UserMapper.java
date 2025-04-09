package com.example.Cloud_file_storage.mapper;

import com.example.Cloud_file_storage.dto.UserResponseDto;
import com.example.Cloud_file_storage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", source = "username")
    UserResponseDto toResponseDto(User user);
}