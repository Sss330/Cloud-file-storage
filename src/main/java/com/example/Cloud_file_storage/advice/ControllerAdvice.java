package com.example.Cloud_file_storage.advice;


import com.example.Cloud_file_storage.exception.auth.LoginAlreadyTakenException;
import com.example.Cloud_file_storage.exception.auth.WrongPasswordException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                //поменять сообщение на более информативное
                .body("{\"message\": \"Bad request\"}");
    }

    @ExceptionHandler(LoginAlreadyTakenException.class)
    public ResponseEntity<String> handleLoginTaken(LoginAlreadyTakenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("{\"message\": \"Username is already taken\"}");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"Invalid username or password\"}");
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<String> handleWrongPassword(WrongPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\": \"Wrong password, or this user doesn't exist\"}");
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Database error occurred\"}");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Oops we`re sorry, unknown error, pls try later again\"}");
    }
}

