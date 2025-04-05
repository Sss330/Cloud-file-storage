package com.example.Cloud_file_storage.advice;

import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SwaggerExceptionAdvice {

    @ExceptionHandler(OpenApiResourceNotFoundException.class)
    public ResponseEntity<String> openApiException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Swagger documentation has been failed\"}");

    }
}
