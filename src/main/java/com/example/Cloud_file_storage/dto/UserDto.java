package com.example.Cloud_file_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
public class UserDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Pls write login")
    @Size(min = 5, max = 20, message = "username must be at least 5 chars and can`t be longer than 20 chars")
    private String username;

    @NotBlank(message = "Pls write password")
    @Size(min = 5, max = 20, message = "password must be at least 5 chars and can`t be longer than 20 chars")
    private String password;

}
