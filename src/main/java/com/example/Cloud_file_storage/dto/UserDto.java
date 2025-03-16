package com.example.Cloud_file_storage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

    @NotBlank(message = "Pls write login")
    private String login;

    @NotBlank(message = "Pls write password")
    private String password;

}
