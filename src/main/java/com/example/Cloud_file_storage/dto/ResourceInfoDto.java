package com.example.Cloud_file_storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfoDto {
    private String path;
    private String name;
    private Long size;
    private String type;
}