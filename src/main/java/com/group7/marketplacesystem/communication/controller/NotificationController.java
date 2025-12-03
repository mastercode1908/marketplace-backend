package com.group7.marketplacesystem.communication.controller;


import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.communication.dto.request.NotificationRequest;
import com.group7.marketplacesystem.communication.dto.response.NotificationResponse;
import com.group7.marketplacesystem.communication.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PostMapping()
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {

        Integer adminId = getCurrentAdminId();

        NotificationResponse response = notificationService.createNotification(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @GetMapping()
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<NotificationResponse> list = notificationService.getAllNotifications();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse>getNotificationById(@PathVariable Integer id){
        NotificationResponse response = notificationService.getNotificationById(id);
        return  ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Đã xóa thành công thông báo "+id);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PutMapping("/{id}")
    public  ResponseEntity<NotificationResponse> updateNotification(@PathVariable Integer id, @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.updateNotification(id, request);
        return  ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PostMapping("/{notificationId}/send/{target}")
    public ResponseEntity<String> sendNotification(
            @PathVariable Integer notificationId,
            @PathVariable String target){
        notificationService.sendNotification(notificationId, target);
        return ResponseEntity.ok("Đã gửi thành công.");
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @GetMapping("/{notificationId}/recipients")
    public ResponseEntity<List<String>> getRecipients(@PathVariable Integer notificationId){
        List<String> recipients = notificationService.getRecipients(notificationId);
        return ResponseEntity.ok(recipients);
    }

    private Integer getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

}
