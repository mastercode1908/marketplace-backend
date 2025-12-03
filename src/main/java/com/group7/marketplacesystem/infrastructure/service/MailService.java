package com.group7.marketplacesystem.infrastructure.service;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productreport;
import com.group7.marketplacesystem.communication.entity.Notification;
import com.group7.marketplacesystem.promotion.entity.Banner;

public interface MailService {

    /**
     * Gửi email xác thực (verify account)
     */
    void sendVerificationEmail(String toEmail, String token);

    /**
     * Gửi email đặt lại mật khẩu (reset password)
     */
    void sendResetPasswordEmail(String toEmail, String token);

    /**
     * Gửi email tùy chỉnh (dùng cho newsletter, promotion,...)
     */
    void sendCustomEmail(String toEmail, String subject, String content);

    void sendReportEmailToAdmin(String toEmail);

    void sendReportEmailToSeller(String toEmail, Productreport productreport);

    void sendResultReportEmailToBuyer(String toEmail, Productreport productreport);

    void sendResultReportEmailToSeller(String toEmail, Productreport productreport);

    void sendProductStatusEmailToSeller(String toEmail, Product product, String note);

    public void sendNotificationEmailToUsers(String toEmail, Notification notification);
    /**
     * Gửi email thông báo seller được duyệt
     */
    void sendSellerApprovalEmail(String toEmail, String shopName);

    /**
     * Gửi email thông báo seller bị từ chối
     */
    void sendSellerRejectionEmail(String toEmail, String shopName, String rejectionNote);

    /**
     * Gửi email thông báo buyer khi seller hủy đơn hàng
     */
    void sendOrderCancelledBySellerEmail(String toEmail, Integer orderId, String sellerName, String reason);

    void sendBannerRejectionEmail(String toEmail , Banner banner, String rejectionReason);

    void sendBannerApprovedEmail(String toEmail, String banner);
}
