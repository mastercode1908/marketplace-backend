package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.identity.dto.request.BuyerUpdateRequest;
import com.group7.marketplacesystem.identity.dto.response.BuyerResponse;
import com.group7.marketplacesystem.identity.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buyer")
public class BuyerController {

    private final BuyerService buyerService;

    @PreAuthorize("hasRole('BUYER')")
    @PatchMapping("/update")
    public ResponseEntity<BuyerResponse> updateBuyer(
            @RequestBody BuyerUpdateRequest buyerUpdateRequest) {

        BuyerResponse updatedBuyer = buyerService.updateBuyer(buyerUpdateRequest);

        return ResponseEntity.ok(updatedBuyer);
    }


    @PreAuthorize("hasRole('BUYER')")
    @GetMapping()
    public ResponseEntity<BuyerResponse> getBuyer() {

        BuyerResponse getBuyer = buyerService.getBuyer();

        return ResponseEntity.ok(getBuyer);
    }
}
