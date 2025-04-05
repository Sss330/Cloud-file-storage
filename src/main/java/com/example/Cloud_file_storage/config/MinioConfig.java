package com.example.Cloud_file_storage.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    @Value("${minio.access-key}")
    String minioUser;

    @Value("${minio.secret-key}")
    String minioPassword;

    @Value("${minio.endpoint}")
    String minioHost;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioHost)
                .credentials(minioUser, minioPassword)
                .build();
    }

}
