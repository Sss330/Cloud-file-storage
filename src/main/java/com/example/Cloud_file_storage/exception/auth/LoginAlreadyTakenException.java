package com.example.Cloud_file_storage.exception.auth;

public class LoginAlreadyTakenException extends RuntimeException {
    public LoginAlreadyTakenException(String message) {
        super(message);
    }
}
