package com.example.Cloud_file_storage.controller;


import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.security.CustomUserDetails;
import com.example.Cloud_file_storage.service.StorageService;
import com.example.Cloud_file_storage.service.storage.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;
    private final FolderService folderService;

    @Operation(summary = "Get info about resource")
    @GetMapping
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String path) throws Exception {
        return ResponseEntity
                .ok()
                .body(storageService.getResourceInfo(path, user.getUser().getId()));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String path) throws Exception {
        storageService.deleteResource(path, user.getUser().getId());
        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(summary = "Downloading zip-file")
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResource(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String path) throws Exception {
        String filename = storageService.getFilenameFromPath(path) + ".zip";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(storageService.downloadResource(path, user.getUser().getId())));
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceInfoDto> moveResource(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String from, String to) throws Exception {
        return ResponseEntity.ok().body(storageService.moveResource(from, to, user.getUser().getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceInfoDto>> searchResource(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String query) throws Exception {
        return ResponseEntity
                .ok()
                .body(storageService.searchResource(query, user.getUser().getId()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceInfoDto>> uploadResource(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String path,
            @RequestParam("object") MultipartFile[] files) throws Exception {
        List<ResourceInfoDto> uploadedResources = new ArrayList<>();
        for (MultipartFile file : files) {
            String filePath = path + file.getOriginalFilename();
            ResourceInfoDto resourceInfo = storageService.uploadResource(filePath, file.getInputStream(), file.getSize(), user.getUser().getId());
            uploadedResources.add(resourceInfo);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedResources);
    }
}
