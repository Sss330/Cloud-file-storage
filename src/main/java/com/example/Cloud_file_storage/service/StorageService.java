package com.example.Cloud_file_storage.service;


import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.exception.common.UnknownException;
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
                return folderService.getFolderInfo(fullPath);
            }
            return fileService.getFileInfo(fullPath);
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundException("Resource not found " + path);
        } catch (Exception e) {
            throw new UnknownException("Failed to get resource info ");
        }
    }

    public void deleteResource(String path) throws Exception {

        try {
            if (isFolder(path)) {
                folderService.deleteFolder(path);
            } else {
                fileService.deleteFile(path);
            }
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundException("Resource not found");
        }
    }

  /*  public ResourceInfoDto moveResource(String from, String to) throws Exception {
        if (!isResourceExists(from)) {
            throw new ResourceNotFoundException("Resource not found " + from);
        }

        if (isResourceExists(to)) {
            throw new ResourceConflictException("Resource already exist  " + to);
        }

        if (isFolder(from)) {
            folderService.moveFolder(from, to);
        } else {
            fileService.moveFile(from, to);
        }

        return getResourceInfo(to);
    }*/

    public InputStream downloadResource(String path) throws Exception {
        if (isFolder(path)) {
            return folderService.downloadFolder(path);
        }
        return fileService.downloadFile(path);
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

    public List<ResourceInfoDto> searchResource(String query) throws Exception {

        List<ResourceInfoDto> resultsOfSearching = new ArrayList<>();
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(query)
                .recursive(true)
                .build());

        for (Result<Item> itemResult : results) {
            Item item = itemResult.get();
            /*if (item.objectName().contains(query)) {
                resultsOfSearching.add(getResourceInfo(item.objectName()));
            }*/
        }
        return resultsOfSearching;
    }

    public ResourceInfoDto uploadResource(String path, InputStream inputStream, Long size, Long id) throws Exception {
        String fullPath = makePathForCurrentUser(path, id);
        client.putObject(PutObjectArgs.builder()
                .stream(inputStream, size, -1)
                .object(fullPath)
                .bucket(bucketName)
                .build());
        return getResourceInfo(fullPath, id);
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

        if (path.contains("/")) {
            String name = path.substring(path.lastIndexOf('/') + 1);
            if (isFolder(path) && name.contains(".")) {
                throw new InvalidPathException("Folder name cannot contain a dot ");
            }
        }

        if (!path.matches("^[а-я-А-Яa-zA-Z0-9_\\-/]+$")) {
            throw new InvalidPathException("Invalid characters in path " + path);
        }
    }
}

