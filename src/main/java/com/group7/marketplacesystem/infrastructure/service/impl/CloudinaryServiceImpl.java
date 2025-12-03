package com.group7.marketplacesystem.infrastructure.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.group7.marketplacesystem.common.config.CloudinaryConfig;
import com.group7.marketplacesystem.infrastructure.dto.response.CloudUploadResponse;
import com.group7.marketplacesystem.infrastructure.mapper.CloudinaryMapper;
import com.group7.marketplacesystem.infrastructure.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryConfig cloudinaryConfig;

    @Override
    public List<CloudUploadResponse> upload(List<MultipartFile> files) {
        List<CloudUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", cloudinaryConfig.getFolder(),
                                "resource_type", "auto"
                        )
                );
                responses.add(CloudinaryMapper.toResponse(uploadResult));
            } catch (IOException e) {
                // Bạn có thể chọn: bỏ file lỗi hoặc throw exception
                throw new RuntimeException("Failed to upload file to Cloudinary: " + file.getOriginalFilename(), e);
            }
        }
        return responses;
    }

    @Override
    public void delete(String publicId, String resourceType) {
        try {
            Map params = ObjectUtils.asMap(
                    "resource_type", resourceType // image, video, raw
            );
            Map result = cloudinary.uploader().destroy(publicId.trim(), params);
            System.out.println("Delete result: " + result);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    @Override
    public CloudUploadResponse uploadSingle(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", cloudinaryConfig.getFolder(),
                            "resource_type", "auto"
                    )
            );

            return CloudinaryMapper.toResponse(uploadResult);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload file to Cloudinary: " + file.getOriginalFilename(), e
            );
        }
    }
}
