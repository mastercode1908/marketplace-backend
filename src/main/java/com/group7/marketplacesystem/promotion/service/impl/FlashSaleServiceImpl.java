package com.group7.marketplacesystem.promotion.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Admin;
import com.group7.marketplacesystem.identity.repository.AdminRepository;
import com.group7.marketplacesystem.promotion.dto.request.FlashSaleRequest;
import com.group7.marketplacesystem.promotion.dto.response.FlashSaleResponse;
import com.group7.marketplacesystem.promotion.entity.Flashsale;
import com.group7.marketplacesystem.promotion.mapper.FlashSaleMapper;
import com.group7.marketplacesystem.promotion.repository.FlashSaleRepository;
import com.group7.marketplacesystem.promotion.service.FlashSaleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@AllArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {
    private final FlashSaleRepository flashSaleRepository;
    private final AdminRepository adminRepository;
    private final FlashSaleMapper flashSaleMapper;

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public List<FlashSaleResponse> getAllFlashSale() {
        return flashSaleRepository.findAll().stream()
                .filter(n -> n.getDeletedAt() == null)
                .map(flashSaleMapper::toResponse)
                .toList();
    }

    @Override
    public FlashSaleResponse createFlashSale(FlashSaleRequest request, Integer adminId) {

        Instant todayStartInstant = LocalDate.now(ZONE_VN).atStartOfDay(ZONE_VN).toInstant();

        Flashsale flashsale = flashSaleMapper.toEntity(request, adminId);

        if (request.getStart_date().isBefore(LocalDateTime.now(ZONE_VN))) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        if (request.getEnd_date().isBefore(request.getStart_date())) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        flashsale.setName(validateAndNormalize(request.getName(), ErrorCode.INVALID_FULLNAME));
        flashsale.setCreatedAt(Instant.now());
        flashsale.setStartDate(request.getStart_date().atZone(ZONE_VN).toInstant());
        flashsale.setEndDate(request.getEnd_date().atZone(ZONE_VN).toInstant());
        flashsale.setUpdatedAt(Instant.now());
        flashsale.setDeletedAt(null);
        Flashsale saved = flashSaleRepository.save(flashsale);

        return flashSaleMapper.toResponse(saved);
    }

    @Override
    public FlashSaleResponse updateFlashSale(Integer id, FlashSaleRequest request) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID flash sale không hợp lệ.");
        }
        Instant todayStartInstant = LocalDate.now(ZONE_VN).atStartOfDay(ZONE_VN).toInstant();

        Flashsale flashsale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FLASHSALE_NOT_FOUND));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            flashsale.setName(validateAndNormalize(request.getName(), ErrorCode.INVALID_FULLNAME));
        }

        if (request.getStart_date().isBefore(LocalDateTime.now(ZONE_VN))) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        if (request.getEnd_date().isBefore(request.getStart_date())) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }
        if (flashsale.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.FLASHSALE_NOT_FOUND);
        }


        if (request.getStart_date() != null) {
            flashsale.setStartDate(request.getStart_date().atZone(ZONE_VN).toInstant());
        }

        if (request.getEnd_date() != null) {
            flashsale.setEndDate(request.getEnd_date().atZone(ZONE_VN).toInstant());
        }

        flashsale.setUpdatedAt(Instant.now());

        Flashsale updated = flashSaleRepository.save(flashsale);
        return flashSaleMapper.toResponse(updated);

    }

    @Override
    public FlashSaleResponse getFlashSaleById(Integer id) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        Flashsale flashsale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FLASHSALE_NOT_FOUND));

        if (flashsale.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.FLASHSALE_NOT_FOUND);
        }

        return flashSaleMapper.toResponse(flashsale);
    }

    @Override
    public void deleteFlashSale(Integer id) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        Flashsale flashsale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FLASHSALE_NOT_FOUND));

        if (flashsale.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.FLASHSALE_NOT_FOUND);
        }

        // Soft delete
        flashsale.setDeletedAt(Instant.now());
        flashSaleRepository.save(flashsale);
    }

    private String validateAndNormalize(String value, ErrorCode error) {
        if (value == null) return null;

        // Kiểm tra space đầu
        if (value.startsWith(" ")) {
            throw new ApiException(error);
        }

        String cleaned = value.trim();
        if (cleaned.isEmpty()) {
            throw new ApiException(error);
        }
        if (cleaned.matches(".*\\s{2,}.*")) {
            throw new ApiException(error); // quá 2 space liên tiếp
        }
        if (!cleaned.matches("^[\\p{L}\\p{N}\\s]+$")) { // chỉ chữ + số + space
            throw new ApiException(error);
        }
        return cleaned;
    }
}
