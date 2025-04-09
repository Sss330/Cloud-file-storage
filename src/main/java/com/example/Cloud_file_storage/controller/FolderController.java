package com.example.Cloud_file_storage.controller;

import com.example.Cloud_file_storage.dto.ResourceInfoDto;
import com.example.Cloud_file_storage.security.CustomUserDetails;
import com.example.Cloud_file_storage.service.storage.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<ResourceInfoDto> createFolder(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String path) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(folderService.createEmptyFolder(path, user.getUser().getId()));
    }

    @GetMapping
    public ResponseEntity<List<ResourceInfoDto>> getFolderContent(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String path) {
        return ResponseEntity
                .ok()
                .body(folderService.getFolderContent(path, user.getUser().getId()));
    }

}
