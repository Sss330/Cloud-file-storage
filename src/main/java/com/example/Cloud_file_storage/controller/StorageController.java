package com.example.Cloud_file_storage.controller;


import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;


    @GetMapping("/path")
    public ResponseEntity<?> getResourcesPath(@RequestParam String path) throws Exception {
        Optional<ResourceInfoDto> dto = storageService.getResourceInfo(path);
        return dto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
