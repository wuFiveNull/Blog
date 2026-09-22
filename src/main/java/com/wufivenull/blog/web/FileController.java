package com.wufivenull.blog.web;

import com.wufivenull.blog.file.FileStorageService;
import com.wufivenull.blog.file.StoredFile;
import com.wufivenull.blog.user.AccountService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class FileController {

    private final FileStorageService storageService;
    private final AccountService accountService;

    public FileController(FileStorageService storageService, AccountService accountService) {
        this.storageService = storageService;
        this.accountService = accountService;
    }

    @PostMapping("/files/upload")
    public Map<String, String> upload(@RequestParam MultipartFile file, Authentication authentication) {
        StoredFile storedFile = storageService.store(file, accountService.current(authentication));
        return Map.of("url", "/files/" + storedFile.getStoredName());
    }

    @GetMapping("/files/{storedName}")
    public ResponseEntity<Resource> get(@PathVariable String storedName) {
        Resource resource = storageService.load(storedName);
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(contentType).body(resource);
    }
}
