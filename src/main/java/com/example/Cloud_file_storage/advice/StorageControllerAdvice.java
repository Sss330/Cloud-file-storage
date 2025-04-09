package com.example.Cloud_file_storage.advice;

import com.example.Cloud_file_storage.exception.auth.UserNotAuthorizedException;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.exception.storage.InvalidPathException;
import com.example.Cloud_file_storage.exception.storage.ResourceConflictException;
import com.example.Cloud_file_storage.exception.storage.ResourceNotFoundException;
import io.minio.errors.MinioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
public class StorageControllerAdvice {

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<String> invalidPathException(InvalidPathException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                body("{\"message\": \"Invalid Path, pls try another path\"}");
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<String> userNotAuthorizedException(UserNotAuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).
                body("{\"message\": \"You ain`t authorized, pls sign up and come back\"}");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> resourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).
                body("{\"message\": \"We`re sorry, but ur resource not found\"}");
    }

    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<String> unknownException(UnknownException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                body("{\"message\": \"Oops server error pls try again later \"}");
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<String> resourceConflictException(ResourceConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\": \"Resource conflict pls try other name or path\"}");
    }

    @ExceptionHandler(MinioException.class)
    public ResponseEntity<String> handleMinioException(MinioException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Storage error: " + e.getMessage() + "\"}");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> genericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Oops we`re sorry, unknown error, pls try later again\"}");
    }
}
