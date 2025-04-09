package com.example.Cloud_file_storage.service.storage;

import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.exception.storage.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final MinioClient client;
    @Value("${minio.bucket}")
    String bucketName;

    public ResourceInfoDto createEmptyFolder(String path, Long id) throws Exception {
        String nameUserFolder = "user-" + id.toString() + "-files/";

        client.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(nameUserFolder + path)
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());

        return ResourceInfoDto.builder()
                .name(getResourceName(nameUserFolder + path))
                .type("DIRECTORY")
                .path(nameUserFolder + path)
                .build();
    }

    public void createUserFolder(Long id) throws Exception {
        String nameUserFolder = "user-" + id.toString() + "-files/";

        if (isFolderExists(nameUserFolder)) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(nameUserFolder)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
            log.info("User folder created: {}", nameUserFolder);
        }

    }

    public ResourceInfoDto getFolderInfo(String path, Long id) {

        try {
            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .maxKeys(1)
                            .build());

            if (results.iterator().hasNext()) {
                return
                        ResourceInfoDto.builder()
                                .path(getParentPath(path, id))
                                .name(getResourceName(path))
                                .type("DIRECTORY")
                                .build();
            } else {
                throw new ResourceNotFoundException("Resource not found by " + path);
            }
        } catch (Exception e) {
            throw new UnknownException("Cat`t get info " + path);
        }
    }

    public List<ResourceInfoDto> getFolderContent(String path, Long id) {

        try {
            path = makePathForCurrentUser(path, id);

            List<ResourceInfoDto> folderContent = new ArrayList<>();
            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(false)
                            .delimiter("/")
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.objectName().equals(path)) continue;

                String parentPath = getParentPath(item.objectName(), id);
                String name = getResourceName(item.objectName());

                ResourceInfoDto resource = ResourceInfoDto.builder()
                        .path(parentPath)
                        .name(name)
                        .type(item.isDir() ? "DIRECTORY" : "FILE")
                        .build();

                if (!item.isDir()) {
                    resource.setSize(item.size());
                }
                folderContent.add(resource);
            }

            return folderContent;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Minio error ");
        }
    }

    public void deleteFolder(String path) throws Exception {
        Iterable<Result<Item>> objects = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(true)
                        .build());

        for (Result<Item> itemResult : objects) {
            Item item = itemResult.get();
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(item.objectName())
                            .build());
        }
    }

    public void moveFolder(String from, String to, Long id) throws Exception {
        from = makePathForCurrentUser(from, id);
        to = makePathForCurrentUser(to, id);


        Iterable<Result<Item>> items = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(from)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> itemResult : items) {
            Item item = itemResult.get();
            String newPath = item.objectName().replace(from, to);
            client.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newPath)
                            .source(CopySource.builder().bucket(bucketName).object(item.objectName()).build())
                            .build()
            );
        }

        deleteFolder(from);
    }

    public InputStream downloadFolder(String path, Long id) throws Exception {
        String normalizedPath = makePathForCurrentUser(path, id);

        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            Iterable<Result<Item>> objects = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(normalizedPath)
                            .recursive(true)
                            .build());


            for (Result<Item> object : objects) {
                Item item = object.get();
                if (!item.isDir()) {
                    String relativePath = item.objectName()
                            .substring(normalizedPath.length());

                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    zipOutputStream.putNextEntry(zipEntry);
                    try (InputStream inputStream = client.getObject(
                            GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .build())) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            zipOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    zipOutputStream.closeEntry();
                }
            }
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public boolean isFolderExists(String folderName) {
        try {
            String normalizedPath = folderName.endsWith("/") ? folderName : folderName + "/";

            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(normalizedPath)
                            .maxKeys(1)
                            .build());
            return !results.iterator().hasNext();
        } catch (Exception e) {
            log.error("Error checking folder exist ");
            return true;
        }
    }

    public String getParentPath(String fullPath, Long userId) {
        String cleanPath = removeUserPrefix(fullPath, userId);

        if (!cleanPath.endsWith("/")) {
            int lastSlash = cleanPath.lastIndexOf('/');
            return lastSlash >= 0 ? cleanPath.substring(0, lastSlash + 1) : "";
        }

        String withoutTrailingSlash = cleanPath.replaceAll("/+$", "");
        int lastSlash = withoutTrailingSlash.lastIndexOf('/');
        return lastSlash >= 0 ? withoutTrailingSlash.substring(0, lastSlash + 1) : "";
    }

    private String makePathForCurrentUser(String path, Long id) {
        return "user-" + id + "-files/" + path;
    }

    private String getResourceName(String path) {
        String cleanPath = path.replaceAll("/+$", "");

        int lastSlash = cleanPath.lastIndexOf('/');
        String name = (lastSlash >= 0) ? cleanPath.substring(lastSlash + 1) : cleanPath;

        return isFolder(path) && !name.isEmpty() ? name + "/" : name;
    }

    private String removeUserPrefix(String fullPath, Long userId) {
        String userPrefix = "user-" + userId + "-files/";
        return fullPath.startsWith(userPrefix)
                ? fullPath.substring(userPrefix.length())
                : fullPath;
    }

    private boolean isFolder(String path) {
        return path.endsWith("/");
    }
}
