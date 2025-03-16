package com.example.Cloud_file_storage.dto.folder;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Folder {
    private final String type = "DIRECTORY";
    private String path;
    private String name;
}
