package com.example.Cloud_file_storage.service;


import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.exception.storage.DeletingResourceException;
import com.example.Cloud_file_storage.exception.storage.InvalidPathException;
import com.example.Cloud_file_storage.exception.storage.ResourceNotFoundException;
import com.example.Cloud_file_storage.service.storage.FileService;
import com.example.Cloud_file_storage.service.storage.FolderService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${minio.bucket}")
    String bucketName;

    private final MinioClient client;
    private final FileService fileService;
    private final FolderService folderService;

    public ResourceInfoDto getResourceInfo(String path, Long id) throws Exception {
        try {
            String fullPath = makePathForCurrentUser(path, id);

            if (isFolder(fullPath)) {
                return folderService.getFolderInfo(fullPath, id);
            }
            return fileService.getFileInfo(fullPath, id);
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundException("Resource not found " + path);
        } catch (Exception e) {
            throw new UnknownException("Failed to get resource info ");
        }
    }

    public void deleteResource(String path, Long id) throws Exception {
        path = makePathForCurrentUser(path, id);
        try {
            if (isFolder(path)) {
                String normalizedPath = path.endsWith("/") ? path : path + "/";
                folderService.deleteFolder(normalizedPath);
            } else {
                client.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
                fileService.deleteFile(path);
            }
        } catch (Exception e) {
            throw new DeletingResourceException("Deleting error with path " + path);
        }
    }

    public ResourceInfoDto moveResource(String from, String to, Long id) throws Exception {
       /* if (!isResourceExists(from)) {
            throw new ResourceNotFoundException("Resource not found " + from);
        }

        if (isResourceExists(to)) {
            throw new ResourceConflictException("Resource already exist  " + to);
        }*/

        if (isFolder(from)) {
            folderService.moveFolder(from, to, id);
        } else {
            fileService.moveFile(from, to, id);
        }

        return getResourceInfo(to, id);
    }

    public InputStream downloadResource(String path, Long id) throws Exception {
        if (isFolder(path)) {
            return folderService.downloadFolder(path, id);
        }
        return fileService.downloadFile(path, id);
    }

    @PostConstruct
    public void initBucket() throws Exception {
        boolean isExist = client.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

        if (!isExist) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket created {}", bucketName);
        }
    }

    public List<ResourceInfoDto> searchResource(String query, Long id) throws Exception {
        if (query.isEmpty() || query.isBlank()) {
            throw new InvalidPathException("Query is invalid, or empty " + query);
        }

        String userPrefix = "user-" + id + "-files/";

        List<ResourceInfoDto> results = new ArrayList<>();
        Iterable<Result<Item>> items = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> itemResult : items) {
            Item item = itemResult.get();
            String objectName = item.objectName();

            if (objectName.startsWith(userPrefix) && objectName.substring(userPrefix.length()).contains(query)) {

                String cleanPath = objectName.substring(userPrefix.length());
                results.add(ResourceInfoDto.builder()
                        .path(getCleanParentPath(cleanPath, id))
                        .name(getResourceName(cleanPath))
                        .size(item.size())
                        .type(item.isDir() ? "DIRECTORY" : "FILE")
                        .build());
            }
        }
        return results;
    }

    public ResourceInfoDto uploadResource(String path, InputStream inputStream, Long size, Long id) throws Exception {
        String fullPath = makePathForCurrentUser(path, id);
        if (isResourceExists(path)) {

        }
        String dsa = getResourceName(fullPath);

        client.putObject(PutObjectArgs.builder()
                .stream(inputStream, size, -1)
                .object(fullPath)
                .bucket(bucketName)
                .build());
        return getResourceInfo(getResourceName(fullPath), id);
    }

    private String getCleanParentPath(String fullPath, Long id) {
        int lastSlash = fullPath.lastIndexOf('/');
        return lastSlash >= 0 ? fullPath.substring(0, lastSlash + 1) : "";
    }

    private String getResourceName(String path) {
        String cleanPath = path.replaceAll("/+$", "");
        int lastSlash = cleanPath.lastIndexOf('/');
        return lastSlash >= 0 ? cleanPath.substring(lastSlash + 1) : cleanPath;
    }


    public String getFilenameFromPath(String path) {
        return path.replace("/", "_").replaceAll("_+$", "");
    }

    private boolean isResourceExists(String path) {
        try {
            client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private String makePathForCurrentUser(String path, Long id) {
        return "user-" + id + "-files/" + path;
    }

    private boolean isFolder(String path) {
        return path.endsWith("/");
    }

    private void validatePath(String path) {

        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Invalid path " + path);
        }

        if (!path.matches("^[a-zA-Z0-9_\\-/]+$")) {
            throw new InvalidPathException("Invalid characters in path " + path);
        }
    }
}

