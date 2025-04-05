package com.example.Cloud_file_storage.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class UserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
}