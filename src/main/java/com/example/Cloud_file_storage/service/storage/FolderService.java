package com.example.Cloud_file_storage.service.storage;

import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import com.example.Cloud_file_storage.exception.storage.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.DeleteObject;
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

    public ResourceInfoDto getFolderInfo(String path) {

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
                                .path(path)
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

    public List<ResourceInfoDto> getFolderContent(String path, Long id) throws Exception {

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

                ResourceInfoDto resource = ResourceInfoDto.builder()
                        .path(item.objectName())
                        .name(getResourceName(item.objectName()))
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

        Iterable<Result<Item>> results = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(true)
                        .build());

        List<DeleteObject> objectsToDelete = new ArrayList<>();
        for (Result<Item> result : results) {
            objectsToDelete.add(new DeleteObject(result.get().objectName()));
        }
        client.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(objectsToDelete)
                        .build());
    }

    public void moveFolder(String from, String to) throws Exception {

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

    public InputStream downloadFolder(String path) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            Iterable<Result<Item>> objects = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(true)
                            .build());

            for (Result<Item> object : objects) {
                Item item = object.get();

                if (!item.isDir()) {
                    ZipEntry zipEntry = new ZipEntry(item.objectName().replace(path, ""));
                    zipOutputStream.putNextEntry(zipEntry);

                    InputStream inputStream = client.getObject(
                            GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .build());
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    zipOutputStream.closeEntry();
                }
            }

        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public boolean isFolderExists(String folderName) {
        try {
            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(folderName)
                            .delimiter("/")
                            .maxKeys(1)
                            .build());
            return results.iterator().hasNext();
        } catch (Exception e) {
            return false;
        }
    }

    private String makePathForCurrentUser(String path, Long id) {
        return "user-" + id + "-files/" + path;
    }

    private String getResourceName(String path) {

        String normalizedPath = path.replaceAll("/+$", "");
        int lastSlashIndex = normalizedPath.lastIndexOf('/');

        if (lastSlashIndex == -1) {
            return normalizedPath;
        }

        return normalizedPath.substring(lastSlashIndex + 1);
    }

    private String normalizedFolderPath(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private String make(String path) {
        return path.endsWith("/") ? path : path + "/";
    }


}
