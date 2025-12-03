package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.SellerRejectRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerReviewService {
    /**
     * Lấy danh sách sellers đang chờ xét duyệt (status = Reviewing)
     */
    Page<SellerReviewResponse> getPendingSellers(Pageable pageable);

    /**
     * Lấy danh sách sellers theo trạng thái
     */
    Page<SellerReviewResponse> getSellersByStatus(String status, Pageable pageable);

    /**
     * Duyệt seller (chuyển status sang Active)
     */
    void approveSeller(Integer sellerId);

    /**
     * Từ chối seller (chuyển status về Incomplete và lưu note)
     */
    void rejectSeller(Integer sellerId, SellerRejectRequest request);
}









