package com.example.Cloud_file_storage.service;

import com.example.Cloud_file_storage.dto.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserResponseDto getUser(UserDetails user) {
        return UserResponseDto.builder()
                .username(user.getUsername())
                .build();
    }
}