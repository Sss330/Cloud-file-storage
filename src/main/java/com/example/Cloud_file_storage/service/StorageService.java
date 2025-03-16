package com.example.Cloud_file_storage.service;


import com.google.common.reflect.ClassPath;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${minio.bucket}")
    String bucketName;


    public Optional<ClassPath.ResourceInfo> getInfoAboutResource(String path) {

        if (path.endsWith("/")) {

        }
        return Optional.empty();
    }

    public void uploadResource() {


    }

    public void renameResource() {


    }

    public void deleteResource() {


    }

}
