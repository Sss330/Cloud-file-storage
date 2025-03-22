package com.example.Cloud_file_storage.dto.file;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class File {
    private String path;
    private String name;
    private Long size;
    private final String type = "FILE";
}
