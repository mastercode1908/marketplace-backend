package com.group7.marketplacesystem.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //Phan hoi loi code tu cai de ben frontend biet thiet lap trang bao loi

    //Global exception
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED(1000, "Validation failed", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    DATABASE_CONSTRAINT_VIOLATION(1003, "Database constraint violation", HttpStatus.CONFLICT),
    BAD_REQUEST(1004, "Bad request", HttpStatus.BAD_REQUEST),
    CART_EMPTY(1005, "Giỏ hàng đang trống", HttpStatus.BAD_REQUEST),




    //Existed
    MAIL_EXISTED(2000, "Mail already exists", HttpStatus.BAD_REQUEST),
    USER_EXISTED(2001, "User already exists", HttpStatus.BAD_REQUEST),
    TAX_EXISTED(2002, "Tax code already exists", HttpStatus.BAD_REQUEST),
    PROMOTION_CODE_EXISTS(2003, "Promotion code already exists", HttpStatus.BAD_REQUEST),



    //Not found
    PROMOTION_NOT_FOUND(3000, "Promotion not found", HttpStatus.NOT_FOUND),
    BUYER_NOT_FOUND(3001, "Buyer not found", HttpStatus.NOT_FOUND),
    SELLER_NOT_FOUND(3002, "Seller not found", HttpStatus.NOT_FOUND),
    ADMIN_NOT_FOUND(3009, "Admin not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(3003, "Product not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(3004, "Order not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND(3005, "Review not found", HttpStatus.NOT_FOUND),
    WISHLIST_NOT_FOUND(3006, "Wishlist not found", HttpStatus.NOT_FOUND),
    SERVICE_PACKAGE_NOT_FOUND(3007, "Service package not found", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(3008, "User does not exist", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(3017, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(3011, "Cart item not found", HttpStatus.NOT_FOUND),
    ADDRESS_NOT_FOUND(3012, "Address not found", HttpStatus.NOT_FOUND),
    DELIVERY_NOT_FOUND(3013, "Delivery not found", HttpStatus.NOT_FOUND),
    COD_AMOUNT_EXCEEDED(3014, "COD amount exceeds the maximum limit of 50,000,000 VND", HttpStatus.BAD_REQUEST),
    QUANTITY_EXCEEDED_STOCK(3015, "The quantity of this product in stock is insufficient. Please check your shopping cart again.", HttpStatus.BAD_REQUEST),
    GHN_SHOP_INFO_NOT_FOUND(3016, "Không tìm thấy thông tin cửa hàng GHN. Vui lòng cấu hình thông tin cửa hàng GHN của cửa hàng trước.", HttpStatus.BAD_REQUEST),
    PRODUCT_REPORT_NOT_FOUND(3017, "Product report not found", HttpStatus.NOT_FOUND),
    GHN_SHOP_NOT_FOUND(3018, "The shop with this Shop ID was not found in GHN. Please check your Token and Shop ID again.", HttpStatus.BAD_REQUEST),
    GHN_TOKEN_INVALID(3019, "GHN Token is invalid or does not have access. Please check Token again.", HttpStatus.BAD_REQUEST),
    ORDER_DETAIL_NOT_FOUND(3030, "Order Detail not found", HttpStatus.NOT_FOUND),
    PRODUCT_IS_DELETE(3031, "Sản phẩm đã bị xóa", HttpStatus.BAD_REQUEST),
    PROMOTION_IS_DELETE(3032, "Phiếu giảm giá đã bị xóa", HttpStatus.BAD_REQUEST),

    FLASHSALE_NOT_FOUND(3009, "Flash sale not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(3010, "Notification not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(3020, "Category not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_IN_CART(3021, "This product is already in your shopping cart.", HttpStatus.BAD_REQUEST),
    PRODUCT_OUT_OF_STOCK(3022, "This product is out of stock", HttpStatus.BAD_REQUEST),
    TOTAL_QUANTITY_EXCEEDED_STOCK(3023, "Tổng số lượng trong giỏ hàng và bạn đang thêm vượt quá số lượng trong kho", HttpStatus.BAD_REQUEST),
    SESSION_NOT_FOUND(1005, "Payment Session not found", HttpStatus.BAD_REQUEST),
    PAYMENT_VNPAY_NOT_FOUND(1006, "Payment VNPAY not found", HttpStatus.BAD_REQUEST),
    BANNER_NOT_FOUND(1007, "Banner is not found", HttpStatus.NOT_FOUND),
    SELLER_PACKAGE_NOT_FOUND(1008,"Seller package is not found" , HttpStatus.NOT_FOUND ),


    //Invalid
    INVALID_USERNAME(4000, "Username must be at least 8 characters and max 20 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(4001, "Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_KEY(4002, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(4003, "Invalid Role", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(4004, "End date must be after start date", HttpStatus.BAD_REQUEST),
    INVALID_START_DATE(4005, "Start date must be after today", HttpStatus.BAD_REQUEST),
    INVALID_END_DATE(4006, "End date must be after today", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_PERCENT(4007, "Discount percent cannot exceed 100%", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(4008, "Phone number should start with 0 and contain 10 or 11 digits.", HttpStatus.BAD_REQUEST),
    INVALID_FULLNAME(4009, "Fullname must be at least 8 characters.", HttpStatus.BAD_REQUEST),
    INVALID_FULLNAME_SYNTAX(4010, "Fullname can only contain letters and spaces.", HttpStatus.BAD_REQUEST),
    IMVALID_PROMOTION_CODE(4011, "Promotion code is empty", HttpStatus.BAD_REQUEST),
    IMVALID_PROMOTION_DESCRIPTION(4012, "Description promotion is empty", HttpStatus.BAD_REQUEST),
    INVALID_SHOP_NAME(4013, "Shop name contains invalid characters.", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(4014, "Address contains invalid characters.", HttpStatus.BAD_REQUEST),
    INVALID_SHOP_DESCRIPTION(4015, "Shop description contains invalid characters.", HttpStatus.BAD_REQUEST),
    INVALID_TITLE(4023, "Title contains invalid characters.", HttpStatus.BAD_REQUEST),
    INVALID_MESSAGE(4024, "Message contains invalid characters.", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY_NAME(4025,"Category name contains invalid or unsupported characters.", HttpStatus.BAD_REQUEST),
    INVALID_DESCRIPTION(4026,"Category description contains invalid or unsupported characters.", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(4027,"Amount is invalid", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY_PROMOTION(4028, "Usage limit must be smaller than 10", HttpStatus.BAD_REQUEST),
    INVALID_BANNER_STATUS(4029,"Banner status is invalid for this action" , HttpStatus.BAD_REQUEST),
    INVALID_BANNER_END_DATE(4039, "Ngày kết thúc phải sau ngày kết thúc gói dịch vụ", HttpStatus.BAD_REQUEST),

    //Password
    PASSWORD_NO_UPPERCASE(5000, "Mật khẩu phải chứa ít nhất 1 chữ hoa", HttpStatus.BAD_REQUEST),
    PASSWORD_NO_LOWERCASE(5001, "Mật khẩu phải chứa ít nhất 1 chữ thường", HttpStatus.BAD_REQUEST),
    PASSWORD_NO_NUMBER(5002, "Mật khẩu phải chứa ít nhất 1 số", HttpStatus.BAD_REQUEST),
    PASSWORD_NO_SPECIAL(5003, "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (@$!%*?&)", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(5004, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(5005, "Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD(5006, "Mật khẩu mới không được trùng với mật khẩu cũ", HttpStatus.BAD_REQUEST),


    //Login
    EMAIL_NOT_VERIFIED(6000, "Email not verified", HttpStatus.UNAUTHORIZED),
    ACCOUNT_BANNED(6001, "Account has been banned", HttpStatus.FORBIDDEN),
    ACCOUNT_INACTIVE(6002, "Account is inactive", HttpStatus.UNAUTHORIZED),
    ACCOUNT_PENDING(6003, "Account is pending", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DELETED(6004, "Account is deleted", HttpStatus.FORBIDDEN),
    ACCOUNT_REVIEWING(6005, "Tài khoản đang được xét duyệt", HttpStatus.UNAUTHORIZED),




    //Validate Promotion code
//    "Mã giảm giá không tồn tại"
    PROMOTION_NOT_FOUND_CHECK(7000, "Promotion code does not exist", HttpStatus.BAD_REQUEST),
    PROMOTION_INACTIVE(7001, "Promotion code is not active", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_STARTED(7002, "Promotion code has not started yet", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED(7003, "Promotion code has expired", HttpStatus.BAD_REQUEST),
    PROMOTION_WRONG_SELLER(7004, "Promotion code is not applicable for this store", HttpStatus.BAD_REQUEST),
    PROMOTION_USAGE_EXCEEDED(7005, "Promotion code usage limit reached", HttpStatus.BAD_REQUEST),
    PROMOTION_ALREADY_USED_BY_BUYER(7006, "You have already used this promotion code", HttpStatus.BAD_REQUEST),
    PROMOTION_MIN_ORDER_NOT_MET(7007, "Order total does not meet the promotion's minimum requirement", HttpStatus.BAD_REQUEST),
    PROMOTION_INVALID_OWNER(7008, "Invalid promotion owner", HttpStatus.BAD_REQUEST),
    PROMOTION_CANNOT_COMBINE(7009, "Cannot apply this promotion with other promotions", HttpStatus.BAD_REQUEST),
    PROMOTION_DELETED(7010, "Promotion code has been deleted or is no longer available", HttpStatus.BAD_REQUEST),


    SERVICE_PACKAGE_STILL_ACTIVE(8000, "Service package is still active", HttpStatus.BAD_REQUEST),
    //Package errors
    PACKAGE_NOT_PURCHASED(8003, "You have not purchased the discount code package", HttpStatus.BAD_REQUEST),
    PACKAGE_EXPIRED(8001, "Your package has expired", HttpStatus.BAD_REQUEST),
    PACKAGE_USAGE_EXCEEDED(8002, "You have used all available discount code creation attempts", HttpStatus.BAD_REQUEST),


    // Chat Message
    CONVERSATION_NOT_FOUND(9000, "Conversation is not found", HttpStatus.BAD_REQUEST),
    SENDER_NOT_FOUND(9001, "Sender is not found", HttpStatus.BAD_REQUEST);


    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
