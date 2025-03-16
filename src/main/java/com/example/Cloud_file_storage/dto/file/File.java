package com.example.Cloud_file_storage.dto.file;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class File {
    private final String type = "FILE";
    private String path;
    private String name;
    private Long size;
}
