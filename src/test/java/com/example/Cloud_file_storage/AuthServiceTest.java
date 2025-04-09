package com.example.Cloud_file_storage;

import com.example.Cloud_file_storage.dto.UserResponseDto;
import com.example.Cloud_file_storage.exception.auth.LoginAlreadyTakenException;
import com.example.Cloud_file_storage.exception.auth.WrongPasswordException;
import com.example.Cloud_file_storage.model.User;
import com.example.Cloud_file_storage.repository.UserRepository;
import com.example.Cloud_file_storage.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class AuthServiceTest {
    @Container


    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void signUp_ShouldCreateUserInDatabase() {
        String username = "Yaroslav";
        String password = "12345";

        UserResponseDto response = authService.signUp(username, password);

        assertNotNull(response);
        assertEquals(username, response.getUsername());

        User user = userRepository.findByUsername(username).orElseThrow();
        assertTrue(passwordEncoder.matches(password, user.getPassword()));
    }

    @Test
    void signUp_WithExistingUsername_ShouldThrowException() {
        String username = "Yaroslav";
        String password = "12345";
        authService.signUp(username, password);

        assertThrows(LoginAlreadyTakenException.class, () -> {
            authService.signUp(username, "anotherpassword");
        });
    }

    @Test
    void signIn_WithValidCredentials_ShouldReturnUserDto() {
        String username = "Yaroslav";
        String password = "12345";
        authService.signUp(username, password);

        UserResponseDto response = authService.signIn(username, password);

        assertNotNull(response);
        assertEquals(username, response.getUsername());
    }

    @Test
    void signIn_WithInvalidPassword_ShouldThrowException() {
        String username = "Yaroslav";
        String password = "12345";
        authService.signUp(username, password);

        assertThrows(WrongPasswordException.class, () -> {
            authService.signIn(username, "wrongpass");
        });
    }
}