package com.group7.marketplacesystem.infrastructure.service;

import com.group7.marketplacesystem.infrastructure.dto.response.CloudUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {

    List<CloudUploadResponse> upload(List<MultipartFile> files);

    void delete(String publicId, String resourceType);

    CloudUploadResponse uploadSingle(MultipartFile file);
}
