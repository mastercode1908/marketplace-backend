package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.promotion.dto.request.ServicePackageRequest;
import com.group7.marketplacesystem.promotion.dto.response.ServicePackageResponse;
import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/service-packages")
@RequiredArgsConstructor
public class ServicePackageController {

    private final ServicePackageService servicePackageService;

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @PostMapping
    public ResponseEntity<ServicePackageResponse> createServicePackage(@Valid @RequestBody ServicePackageRequest request) {
        Integer adminId = getCurrentAdminId();

        ServicePackageResponse response = servicePackageService.createServicePackage(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ServicePackageResponse> getServicePackageById(@PathVariable Integer id) {
        ServicePackageResponse response = servicePackageService.getServicePackageById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @GetMapping
    public ResponseEntity<Page<ServicePackageResponse>> getAllServicePackages(Pageable pageable) {
        Page<ServicePackageResponse> responsePage = servicePackageService.getAllServicePackages(pageable);
        return ResponseEntity.ok(responsePage);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServicePackageResponse> updateServicePackage(@PathVariable Integer id, @Valid @RequestBody ServicePackageRequest request) {
        ServicePackageResponse response = servicePackageService.updateServicePackage(id, request);
        return ResponseEntity.ok(response);
    }

//    @PreAuthorize("hasAuthority('ROLE_SYSTEMADMIN')")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteServicePackage(@PathVariable Integer id) {
        servicePackageService.deleteServicePackage(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotion deleted successfully");
        response.put("promotionId", id.toString());

        return ResponseEntity.ok(response);
    }

    private Integer getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

}
