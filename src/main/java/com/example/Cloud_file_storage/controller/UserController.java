package com.example.Cloud_file_storage.controller;

import com.example.Cloud_file_storage.dto.UserResponseDto;
import com.example.Cloud_file_storage.security.CustomUserDetails;
import com.example.Cloud_file_storage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok().body(userService.getUser(user));
    }
}
