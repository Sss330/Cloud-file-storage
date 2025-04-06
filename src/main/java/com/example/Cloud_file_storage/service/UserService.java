package com.example.Cloud_file_storage.service;

import com.example.Cloud_file_storage.dto.UserResponseDto;
import com.example.Cloud_file_storage.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    public UserResponseDto getUser(CustomUserDetails user) {
        return UserResponseDto.builder()
                .username(user.getUsername())
                .build();
    }
}