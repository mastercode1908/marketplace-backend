package com.group7.marketplacesystem.catalog.service.impl;

import com.group7.marketplacesystem.catalog.dto.request.ProductReportRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductReportResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productreport;
import com.group7.marketplacesystem.catalog.mapper.ProductReportMapper;
import com.group7.marketplacesystem.catalog.repository.ProductReportRepository;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.service.ProductReportService;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReportServiceImpl implements ProductReportService {

    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;
    private final ProductReportRepository productReportRepository;
    private final ProductReportMapper  productReportMapper;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final OrderDetailRepository  orderDetailRepository;

    @Override
    public ProductReportResponse creatProductReport(ProductReportRequest dto) {
        System.out.println("Creating product report for product: " + dto.getProductId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int buyerId = userDetails.getUser().getId();

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        Productreport report = new Productreport();
        report.setProduct(product);
        report.setBuyer(buyer);
        report.setReason(dto.getReason());
        report.setStatus("Pending");
        report.setCreatedAt(Instant.now());
        report.setResolvedAt(null);

        if (dto.getMedia() != null && !dto.getMedia().isEmpty()) {
            System.out.println("Processing media list of size: " + dto.getMedia().size());
            List<com.group7.marketplacesystem.catalog.entity.ReportMedia> mediaList = dto.getMedia().stream()
                    .map(mediaDTO -> {
                        com.group7.marketplacesystem.catalog.entity.ReportMedia media = new com.group7.marketplacesystem.catalog.entity.ReportMedia();
                        media.setUrl(mediaDTO.getUrl());
                        media.setPublicId(mediaDTO.getPublicId());
                        media.setMediaType(mediaDTO.getMediaType());
                        media.setReport(report);
                        return media;
                    })
                    .collect(Collectors.toList());
            report.setMedia(mediaList);
        } else {
            System.out.println("No media provided in request");
        }

        try {
            productReportRepository.save(report);
            System.out.println("Report saved successfully with ID: " + report.getId());
        } catch (Exception e) {
            System.err.println("Error saving report: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        Orderdetail orderdetail = orderDetailRepository.findById(dto.getOrderDetailId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
        orderdetail.setIsReported(true);
        orderDetailRepository.save(orderdetail);

        List<User> contentAdmins = userRepository.findAllByRole("CONTENTADMIN");

        // 2. Lấy danh sách email
        List<String> emails = contentAdmins.stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();

        // 3. Gửi email cho từng admin
        for (String email : emails) {
            mailService.sendReportEmailToAdmin(email);
        }
        mailService.sendReportEmailToSeller(product.getSeller().getUsers().getEmail(), report);

        return productReportMapper.toResponse(report);
    }

    @Override
    public List<ProductReportResponse> getAllProductReportsByBuyerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int buyerId = userDetails.getUser().getId();
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        List<Productreport> reports = productReportRepository.findByBuyerId(buyerId);

        return reports.stream()
                .map(productReportMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductReportResponse> getAllProductReports() {
        List<Productreport> reports = productReportRepository.findAll();

        return reports.stream()
                .map(productReportMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductReportResponse updateProductReport(Long id, ProductReportRequest dto) {
        Productreport productreport = productReportRepository.findById(Integer.parseInt(id.toString()))
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_REPORT_NOT_FOUND));

        productreport.setStatus(dto.getStatus());
        productreport.setResolvedAt(Instant.now());

        productReportRepository.save(productreport);

        Product product = productreport.getProduct();

        if(dto.getStatus().equals("Resolved")) {
            product.setProductStatus("Inactive");
            productRepository.save(product);
        }

        try {
            if (productreport.getBuyer() != null && productreport.getBuyer().getUsers() != null) {
                mailService.sendResultReportEmailToBuyer(productreport.getBuyer().getUsers().getEmail(), productreport);
            }
            if (product.getSeller() != null && product.getSeller().getUsers() != null) {
                mailService.sendResultReportEmailToSeller(product.getSeller().getUsers().getEmail(), productreport);
            }
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to send report email: " + e.getMessage());
        }

        return productReportMapper.toResponse(productreport);
    }
}
