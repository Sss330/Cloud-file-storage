package com.example.Cloud_file_storage.service;


import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.exception.storage.InvalidPathException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    String bucketName;

    private final MinioClient client;

    public Optional<ResourceInfoDto> getResourceInfo(String path) throws Exception {
        validatePath(path);

        if (isFolder(path)) {
            return getFolderInfo(path);
        } else {
            return getFileInfo(path);
        }
    }

    @PostConstruct
    public void initBucket() throws Exception {
        boolean isExist = client.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

        if (!isExist) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket {} created", bucketName);
        }
    }

    public void createUserFolder(Long id) throws Exception {
        String nameUserFolder = "user-" + id + "-files/";

        if (!isFolderExists(nameUserFolder)) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(nameUserFolder)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
            log.info("User folder created: {}", nameUserFolder);
        }

    }

    private void validatePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Invalid path: " + path);
        }
    }

    private Optional<ResourceInfoDto> getFileInfo(String path) throws Exception {
        try {
            StatObjectResponse stat = client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            return Optional.of(ResourceInfoDto.builder()
                    .path(path)
                    .name(getResourceName(path))
                    .size(stat.size())
                    .type("FILE")
                    .build());
        } catch (ErrorResponseException e) {
            return Optional.empty();
        }
    }

    private Optional<ResourceInfoDto> getFolderInfo(String path) {
        try {
            if (!path.endsWith("/")) {
                path = path + "/";
            }

            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .maxKeys(1)
                            .build());

            if (results.iterator().hasNext()) {
                return Optional.of(ResourceInfoDto.builder()
                        .path(path)
                        .name(getResourceName(path))
                        .type("DIRECTORY")
                        .build());
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getResourceName(String path) {

        String normalizedPath = path.replaceAll("/+$", "");
        int lastSlashIndex = normalizedPath.lastIndexOf('/');

        if (lastSlashIndex == -1) {
            return normalizedPath;
        }

        return normalizedPath.substring(lastSlashIndex + 1);
    }


    private boolean isFolder(String path) {
        return path.endsWith("/");
    }

    private boolean isFolderExists(String folderName) {
        try {
            client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderName + "/")
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

