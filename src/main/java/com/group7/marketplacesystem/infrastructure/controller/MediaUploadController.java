package com.group7.marketplacesystem.infrastructure.controller;

import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.infrastructure.dto.response.CloudUploadResponse;
import com.group7.marketplacesystem.infrastructure.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaUploadController {

    private final CloudinaryService cloudinaryService;

    // Upload single file
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<CloudUploadResponse>>> uploadMultiple(@RequestParam("file") List<MultipartFile> files) {
        List<CloudUploadResponse> response = cloudinaryService.upload(files);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.MEDIA_CREATE_SUCCESS,response));
    }

    // Delete by publicId
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> delete(
            @RequestParam String publicId,
            @RequestParam String resourceType // "image", "video", "raw"
    ) {
        cloudinaryService.delete(publicId.trim(), resourceType);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.MEDIA_DELETE_SUCCESS));
    }

    @PostMapping("/upload/single")
    public ResponseEntity<ApiResponse<CloudUploadResponse>> uploadSingle(@RequestParam("file") MultipartFile file) {
        CloudUploadResponse response = cloudinaryService.uploadSingle(file);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.MEDIA_CREATE_SUCCESS, response));
    }
}
