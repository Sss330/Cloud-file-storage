package com.example.Cloud_file_storage.advice;

import com.example.Cloud_file_storage.exception.auth.AccessDeniedException;
import com.example.Cloud_file_storage.exception.auth.LoginAlreadyTakenException;
import com.example.Cloud_file_storage.exception.auth.WrongPasswordException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice()
public class AuthControllerAdvice {

    @ExceptionHandler(LoginAlreadyTakenException.class)
    public ResponseEntity<String> loginTakenException(LoginAlreadyTakenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("{\"message\": \"Username is already taken\"}");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> badCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"Invalid username or password\"}");
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<String> wrongPasswordException(WrongPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"Wrong password, or this user doesn't exist\"}");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> dataAccessException(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Database operation failed, Please try again later\"}");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> genericException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"Access denied, u`r not authorized\"}");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorDetails = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(", "));

        String responseMessage = String.format("%s", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"message\": \"" + responseMessage + "\"}");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> genericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Oops we`re sorry, unknown error, pls try later again\"}");
    }
}