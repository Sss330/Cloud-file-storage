package com.example.Cloud_file_storage.exception.auth;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
