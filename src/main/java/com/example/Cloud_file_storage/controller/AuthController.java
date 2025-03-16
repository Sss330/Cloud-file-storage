package com.example.Cloud_file_storage.controller;

import com.example.Cloud_file_storage.dto.UserDto;
import com.example.Cloud_file_storage.mapper.UserMapper;
import com.example.Cloud_file_storage.model.User;
import com.example.Cloud_file_storage.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody UserDto userDto) {

        User user = authService.signUp(userDto.getLogin(), userDto.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(user));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> signIn(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity
                .ok()
                .body(userMapper.toDto(authService.signIn(userDto.getLogin(), userDto.getPassword())));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<String> logout(HttpSession session) {
        authService.logOut(session);
        return ResponseEntity.noContent().build();
    }

}
