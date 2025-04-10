package com.example.Cloud_file_storage.service.storage;

import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.exception.common.BadRequestException;
import com.example.Cloud_file_storage.exception.common.UnknownException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient client;
    private final FolderService folderService;
    @Value("${minio.bucket}")
    String bucketName;

    public ResourceInfoDto getFileInfo(String path, Long id) {
        try {
            if (path == null || path.isEmpty()) {
                throw new BadRequestException("Invalid path: " + path);
            }

            StatObjectResponse stat = client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            return
                    ResourceInfoDto.builder()
                            .path(folderService.getParentPath(path, id))
                            .name(getResourceName(path))
                            .size(stat.size())
                            .type("FILE")
                            .build();
        } catch (Exception e) {
            throw new UnknownException("Can`t get file info " + path);
        }
    }

    public void deleteFile(String path) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }

    public InputStream downloadFile(String path, Long id) throws Exception {
        path = makePathForCurrentUser(path, id);
        return client.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }

    public void moveFile(String from, String to, Long id) throws Exception {
        from = makePathForCurrentUser(from, id);
        to = makePathForCurrentUser(to, id);

        client.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(to)
                        .source(CopySource.builder().bucket(bucketName).object(from).build())
                        .build()
        );

        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(from)
                        .build()
        );
    }

    private String makePathForCurrentUser(String path, Long id) {
        return "user-" + id + "-files/" + path;
    }

    private String getResourceName(String path) {
        String validPath = path.replaceAll("/+$", "");
        int lastSlashIndex = validPath.lastIndexOf('/');

        if (lastSlashIndex == -1) {
            return validPath;
        }
        return validPath.substring(lastSlashIndex + 1);
    }

}
