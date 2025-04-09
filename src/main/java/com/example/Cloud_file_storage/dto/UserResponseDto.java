package com.example.Cloud_file_storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class UserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
}