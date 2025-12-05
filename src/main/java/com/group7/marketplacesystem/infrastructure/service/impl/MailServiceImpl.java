package com.group7.marketplacesystem.infrastructure.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productreport;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.entity.Notification;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import com.group7.marketplacesystem.promotion.entity.Banner;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class MailServiceImpl{

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}") // link frontend mặc định
    private String frontendUrl;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

//    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/verify?token=" + token;
        String subject = "Xác thực tài khoản của bạn";
        String content = """
                Xin chào,
                
                Cảm ơn bạn đã đăng ký tài khoản. 
                Vui lòng nhấp vào liên kết bên dưới để xác thực email của bạn:
                
                %s
                
                Liên kết này sẽ hết hạn sau 15 phút.
                
                Trân trọng,
                Đội ngũ Online MarketPlace
                """.formatted(verifyLink);

        sendCustomEmail(toEmail, subject, content);
    }

//    @Override
    public void sendResetPasswordEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "Đặt lại mật khẩu";
        String content = """
                Xin chào,
                
                Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản của mình. 
                Vui lòng truy cập liên kết bên dưới để tạo mật khẩu mới:
                
                %s
                
                Liên kết này sẽ hết hạn sau 15 phút.
                
                Nếu bạn không yêu cầu, vui lòng bỏ qua email này.
                
                Trân trọng,
                Đội ngũ Online MarketPlace
                """.formatted(resetLink);

        sendCustomEmail(toEmail, subject, content);
    }

//    @Override
    public void sendCustomEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error sending email", e);
        }
    }

    public void sendReportEmailToAdmin(String toEmail) {
        String subject = "Có báo cáo sản phẩm mới";

        // Link điều hướng vào trang quản lý report
        String manageReportLink = frontendUrl + "/content-admin/reports";

        String content = """
            Xin chào Admin,

                Hệ thống vừa ghi nhận một báo cáo sản phẩm mới.
                Vui lòng truy cập trang quản lý báo cáo để xem chi tiết và xử lý.

                %s

                Trân trọng, 
                Hệ thống Online Marketplace
            """.formatted(manageReportLink);

        sendCustomEmail(toEmail, subject, content);
    }

//    @Override
    public void sendReportEmailToSeller(String toEmail, Productreport productreport) {
        if (productreport == null || productreport.getProduct() == null) {
            throw new ApiException(ErrorCode.PRODUCT_REPORT_NOT_FOUND);
        }

        String productName = productreport.getProduct().getName();
        String reason = productreport.getReason();

        String subject = "Sản phẩm của bạn đã bị báo cáo";

        String content = """
            Xin chào,

            Sản phẩm của bạn đã bị người mua báo cáo.

            Tên sản phẩm: %s
            Lý do báo cáo: %s

            Vui lòng truy cập trang quản lý để kiểm tra chi tiết và xử lý kịp thời(Trong vòng 3 ngày).

            Trân trọng,
            Hệ thống Online Marketplace
            """.formatted(productName, reason);

        sendCustomEmail(toEmail, subject, content);
    }


//    @Override
    public void sendResultReportEmailToBuyer(String toEmail, Productreport productreport) {

        if (productreport == null || productreport.getProduct() == null) {
            throw new ApiException(ErrorCode.PRODUCT_REPORT_NOT_FOUND);
        }

        String productName = productreport.getProduct().getName();
        String reason = productreport.getReason();
        String status = productreport.getStatus();

        String subject = "Kết quả báo cáo sản phẩm của bạn";

        String content = """
            Xin chào,

            Báo cáo của bạn về một sản phẩm đã được hệ thống xử lý.

            Tên sản phẩm: %s
            Lý do báo cáo: %s
            Kết quả xử lý: %s

            Cảm ơn bạn đã giúp chúng tôi cải thiện chất lượng sản phẩm trên Marketplace.

            Trân trọng,
            Hệ thống Online Marketplace
            """.formatted(productName, reason, status);

        sendCustomEmail(toEmail, subject, content);
    }


//    @Override
    public void sendResultReportEmailToSeller(String toEmail, Productreport productreport) {
        if (productreport == null || productreport.getProduct() == null) {
            throw new ApiException(ErrorCode.PRODUCT_REPORT_NOT_FOUND);
        }

        String productName = productreport.getProduct().getName();
        String reason = productreport.getReason();
        String status = productreport.getStatus();

        String subject = "Kết quả xử lý báo cáo sản phẩm của bạn";

        String content = """
            Xin chào Seller,

            Báo cáo về sản phẩm của bạn đã được hệ thống xử lý.

            Tên sản phẩm bị báo cáo: %s
            Lý do buyer báo cáo: %s
            Kết quả xử lý: %s

            Vui lòng xem lại chất lượng sản phẩm và điều chỉnh nếu cần thiết
            để tránh các báo cáo tương tự trong tương lai.

            Trân trọng,
            Hệ thống Online Marketplace
            """.formatted(productName, reason, status);

        sendCustomEmail(toEmail, subject, content);
    }

    public void sendProductStatusEmailToSeller(String toEmail, Product product, String note) {
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        String productName = product.getName();
        String status = product.getProductStatus();

        String subject = "Thông báo cập nhật trạng thái sản phẩm";

        String content = """
        Xin chào Seller,

        Sản phẩm của bạn vừa được hệ thống cập nhật trạng thái.

        Tên sản phẩm: %s
        Trạng thái mới: %s
        Ghi chú từ hệ thống: %s

        Vui lòng kiểm tra và điều chỉnh nếu cần thiết để đảm bảo tuân thủ chính sách.

        Trân trọng,
        Hệ thống Online Marketplace
        """.formatted(productName, status, note);

        sendCustomEmail(toEmail, subject, content);
    }

//    @Async
//    @Override
    public void sendNotificationEmailToUsers(String toEmail, Notification notification) {
        String subject = "[Thông báo từ Marketplace System] " + notification.getTitle();
        String shopLink = frontendUrl + "/home";
        String content = """
        <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h1 style=" font-size: 28px; font-weight: 600; color: #0078D4; margin: 0 0 20px 0; "> %s</h1>
                <p>Xin chào,</p>
                <p>Bạn vừa nhận được một thông báo mới từ hệ thống Marketplace:</p>

                <div style="background: #f5f5f5; padding: 12px; border-radius: 6px;">
                    <p style="margin: 0;"><strong>Nội dung:</strong> %s</p>
                </div>

                <p>Vui lòng truy cập trang web của cửa hàng để xem chi tiết:</p>

                <a href="%s"
                   style="display:inline-block; padding:10px 18px; background:#2d6cdf; color:#fff;
                   text-decoration:none; border-radius:6px; margin-top:10px;">
                    Xem thông báo
                </a>

                <p style="margin-top:20px;">Trân trọng,<br>Marketplace System</p>
            </body>
        </html>
    """.formatted(
                notification.getTitle(),
                notification.getMessage(),
                shopLink);
        sendCustomEmail(toEmail, subject, content);
    }
    public void sendSellerApprovalEmail(String toEmail, String shopName) {
        String subject = "Đơn đăng ký seller của bạn đã được duyệt";
        String content = """
                Xin chào,
                
                Chúc mừng! Đơn đăng ký seller của bạn đã được hệ thống xét duyệt và chấp nhận.
                
                Tên cửa hàng: %s
                
                Bạn có thể bắt đầu bán hàng trên Marketplace ngay bây giờ.
                Vui lòng đăng nhập vào tài khoản để quản lý cửa hàng và sản phẩm.
                
                Trân trọng,
                Đội ngũ Online MarketPlace
                """.formatted(shopName != null ? shopName : "Cửa hàng của bạn");

        sendCustomEmail(toEmail, subject, content);
    }

//    @Override
    public void sendSellerRejectionEmail(String toEmail, String shopName, String rejectionNote) {
        String subject = "Thông báo về đơn đăng ký seller";
        String content = """
                Xin chào,
                
                Rất tiếc, đơn đăng ký seller của bạn đã bị từ chối.
                
                Tên cửa hàng: %s
                Lý do từ chối: %s
                
                Vui lòng kiểm tra lại thông tin và điền lại form đăng ký seller.
                Sau khi hoàn thiện, bạn có thể gửi lại đơn đăng ký để được xét duyệt.
                
                Trân trọng,
                Đội ngũ Online MarketPlace
                """.formatted(
                shopName != null ? shopName : "Cửa hàng của bạn",
                rejectionNote != null ? rejectionNote : "Không đáp ứng yêu cầu"
        );

        sendCustomEmail(toEmail, subject, content);
    }

//    @Override
    public void sendOrderCancelledBySellerEmail(String toEmail, Integer orderId, String sellerName, String reason) {
        String subject = "Thông báo: Đơn hàng #" + orderId + " đã bị hủy bởi người bán";
        String orderLink = frontendUrl + "/user/orders/" + orderId;
        String content = """
                Xin chào,
                
                Chúng tôi rất tiếc phải thông báo rằng đơn hàng #%d của bạn đã bị hủy bởi người bán.
                
                Thông tin đơn hàng:
                - Mã đơn hàng: #%d
                - Người bán: %s
                - Lý do hủy đơn: %s
                
                Nếu bạn đã thanh toán, số tiền sẽ được hoàn trả vào tài khoản của bạn trong vòng 3-5 ngày làm việc.
                
                Bạn có thể xem chi tiết đơn hàng tại:
                %s
                
                Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.
                
                Trân trọng,
                Đội ngũ Online MarketPlace
                """.formatted(
                orderId,
                orderId,
                sellerName != null ? sellerName : "Người bán",
                reason != null && !reason.trim().isEmpty() ? reason : "Không có lý do cụ thể",
                orderLink
        );

        sendCustomEmail(toEmail, subject, content);
    }

//    @Async
//    @Override
    public void sendBannerRejectionEmail(String toEmail, Banner banner, String rejectionReason) {

        if (banner == null) {
            throw new ApiException(ErrorCode.BANNER_NOT_FOUND);
        }

        String bannerTitle = banner.getTitle() != null ? banner.getTitle() : "Banner của bạn";
        String reason = (rejectionReason != null && !rejectionReason.trim().isEmpty())
                ? rejectionReason
                : "Không có lý do cụ thể";

        String subject = "Thông báo: Banner của bạn đã bị từ chối";

        String content = """
            Xin chào Seller,
            
            Banner của bạn đã được hệ thống kiểm duyệt và *không được chấp nhận*.
            
            Thông tin banner:
            - Tiêu đề banner: %s
            - Lý do từ chối: %s
            
            Vui lòng kiểm tra lại nội dung banner và điều chỉnh theo đúng quy định.
            Sau khi cập nhật, bạn có thể gửi lại để xét duyệt.
            
            Trân trọng,
            Hệ thống Online Marketplace
            """.formatted(
                bannerTitle,
                reason
        );

        sendCustomEmail(toEmail, subject, content);
    }

//    @Async
//    @Override
    public void sendBannerApprovedEmail(String toEmail, String banner) {
        String subject = "Đơn đăng ký banner quảng cáo của bạn đã được duyệt";
        String content = """
        Xin chào,

        Chúc mừng! Đơn đăng ký banner quảng cáo của bạn đã được hệ thống xét duyệt và chấp nhận.

        • Tên banner: %s

        Bạn có thể bắt đầu quản lý và sử dụng banner quảng cáo của mình ngay bây giờ trên Marketplace.
        Vui lòng đăng nhập vào tài khoản để quản lý banner và các nội dung khác liên quan.

        Trân trọng,
        Đội ngũ Online Marketplace
        """.formatted(banner != null ? banner : "Banner của bạn");

        sendCustomEmail(toEmail, subject, content);
    }

}
