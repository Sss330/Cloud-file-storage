package com.example.Cloud_file_storage.service;

import com.example.Cloud_file_storage.dto.UserResponseDto;
import com.example.Cloud_file_storage.exception.auth.LoginAlreadyTakenException;
import com.example.Cloud_file_storage.exception.auth.WrongPasswordException;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.mapper.UserMapper;
import com.example.Cloud_file_storage.model.User;
import com.example.Cloud_file_storage.repository.UserRepository;
import com.example.Cloud_file_storage.security.CustomUserDetails;
import com.example.Cloud_file_storage.service.storage.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;
    private final FolderService folderService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto signUp(String username, String password) {
        if (userRepository.existsUserByUsername(username)) {
            throw new LoginAlreadyTakenException("Login already taken ");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        userRepository.save(user);

        try {
            folderService.createUserFolder(user.getId());
        } catch (Exception e) {
            userRepository.delete(user);
            throw new UnknownException("Folder not created ");
        }

        authUser(username, password);
        return userMapper.toResponseDto(user);
    }

    public UserResponseDto signIn(String login, String password) {
        try {
            authUser(login, password);
            User user = getAuthenticatedUser();
            return userMapper.toResponseDto(user);
        } catch (BadCredentialsException e) {
            throw new WrongPasswordException("Invalid credentials");
        }
    }

    public void authUser(String login, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }

    private User getAuthenticatedUser() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }
}
