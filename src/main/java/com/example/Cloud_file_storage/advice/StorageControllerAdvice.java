package com.example.Cloud_file_storage.advice;

import com.example.Cloud_file_storage.exception.auth.UserNotAuthorizedException;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.exception.storage.InvalidPathException;
import com.example.Cloud_file_storage.exception.storage.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StorageControllerAdvice {

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<String> invalidPathException(InvalidPathException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                body("{\"message\": \"Invalid Path, pls try another path\"}");
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<String> invalidPathException(UserNotAuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).
                body("{\"message\": \"You ain`t authorized, pls sign up and come back\"}");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> invalidPathException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).
                body("{\"message\": \"We`re sorry, but ur resource not found\"}");
    }

    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<String> invalidPathException(UnknownException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                body("{\"message\": \"Oops server error pls try again later \"}");
    }

}
