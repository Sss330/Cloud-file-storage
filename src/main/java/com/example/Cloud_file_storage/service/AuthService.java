package com.example.Cloud_file_storage.service;


import com.example.Cloud_file_storage.exception.auth.LoginAlreadyTakenException;
import com.example.Cloud_file_storage.exception.auth.WrongPasswordException;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.model.User;
import com.example.Cloud_file_storage.repository.UserRepository;
import com.example.Cloud_file_storage.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User signUp(String login, String password) {

        try {
            if (userRepository.existsUserByLogin(login)) {
                throw new LoginAlreadyTakenException("");
            }

            User user = User.builder()
                    .login(login)
                    .password(passwordEncoder.encode(password))
                    .build();
            userRepository.save(user);
            log.info("User saved by login {}", user);

            authUser(login, password);
            return user;

        } catch (UnknownException e) {
            throw new UnknownException("");
        }
    }

    public User signIn(String login, String password) {
        try {
            authUser(login, password);
            return getAuthenticatedUser();
        } catch (BadCredentialsException e) {
            throw new WrongPasswordException("Invalid credentials");
        }
    }

    public void logOut(HttpSession session) {
        try {
            session.invalidate();
            SecurityContextHolder.clearContext();
            log.info("User logged out successfully");
        } catch (Exception e) {
            throw new UnknownException("Logout failed");
        }

    }

    private void authUser(String login, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User getAuthenticatedUser() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }
}
