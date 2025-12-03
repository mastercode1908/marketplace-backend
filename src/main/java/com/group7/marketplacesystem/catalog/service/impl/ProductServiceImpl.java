package com.group7.marketplacesystem.catalog.service.impl;

import com.group7.marketplacesystem.catalog.dto.request.ProductRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductDetailResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductResponse;
import com.group7.marketplacesystem.catalog.dto.request.ProductCreateRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductInfoResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.catalog.mapper.ProductMapper;
import com.group7.marketplacesystem.catalog.mapper.ProductMediaMapper;
import com.group7.marketplacesystem.catalog.repository.CategoryRepository;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import com.group7.marketplacesystem.catalog.service.ProductService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CurrentUser;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import com.group7.marketplacesystem.promotion.entity.PackageUsage;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import com.group7.marketplacesystem.promotion.entity.Servicepackage;
import com.group7.marketplacesystem.promotion.repository.PackageUsageRepository;
import com.group7.marketplacesystem.promotion.repository.SellerPackageRepository;
import com.group7.marketplacesystem.promotion.repository.ServicePackageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductmediaRepository productMediaRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final ProductMapper productMapper;
    private final PackageUsageRepository pkgUsageRepository;
    private final SellerPackageRepository sellerPackageRepository;
    private final ServicePackageRepository  servicePackageRepository;
    private final MailService mailService;

    @Override
    public ProductInfoResponse createProduct(ProductCreateRequest request) {
        // Lấy seller từ security context
        Integer sellerId = CurrentUser.getUserId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        // Lấy category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        // Map sang Product
        Product product = ProductMapper.toEntity(request, seller, category);

        // Lưu Product
        productRepository.save(product);

        // Map và lưu ProductMedia nếu có
        List<Productmedia> mediaList = ProductMediaMapper.toEntityList(product, request.getMedia());
        if (!mediaList.isEmpty()) {
            productMediaRepository.saveAll(mediaList);
        }

        // Trả về response
        return ProductMapper.toResponse(product, mediaList);
    }

    @Override
    public ProductInfoResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        // Lấy media
        List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(productId);

        return ProductMapper.toResponse(product, mediaList);
    }

    @Override
    public ProductInfoResponse updateProduct(Integer productId, ProductCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        // Check quyền seller
        Integer sellerId = CurrentUser.getUserId();
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy category nếu muốn update
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        // Map lại product (dùng mapper update luôn)
        ProductMapper.updateEntity(product, request, category);
        productRepository.save(product);

        // Xử lý media (có thể xóa cũ, thêm mới)
        // Hiện tại: thêm mới media từ request
        List<Productmedia> oldMedia = productMediaRepository.findByProductIdAndDeletedAtIsNull(productId);
        for (Productmedia m : oldMedia) {
            m.setDeletedAt(Instant.now());
        }
        productMediaRepository.saveAll(oldMedia);

        List<Productmedia> mediaList = ProductMediaMapper.updateMedia(product, request.getMedia());
        if (!mediaList.isEmpty()) {
            productMediaRepository.saveAll(mediaList);
        }

        return ProductMapper.toResponse(product, mediaList);
    }

    @Override
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        // Check quyền seller
        Integer sellerId = CurrentUser.getUserId();
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Soft delete product
        product.setDeletedAt(Instant.now());
        productRepository.save(product);

        // Soft delete media
        List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(productId);
        for (Productmedia media : mediaList) {
            media.setDeletedAt(Instant.now());
        }
        productMediaRepository.saveAll(mediaList);
    }

    @Override
    public List<ProductInfoResponse> getMyProducts() {
        Integer sellerId = CurrentUser.getUserId();

        List<Product> products = productRepository.findBySellerIdAndDeletedAtIsNullOrderByIdDesc(sellerId);

        return products.stream().map(p -> {
            List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(p.getId());
            return ProductMapper.toResponse(p, mediaList);
        }).toList();
    }

    @Override
    public List<ProductDetailResponse> getAllPendingProduct() {
        return getProductsByStatus("Pending");
    }

    @Override
    public List<ProductDetailResponse> getProductsByStatus(String status) {
        if (status == null || status.isEmpty() || "ALL".equalsIgnoreCase(status)) {
            return productRepository.findAll()
                    .stream()
                    .filter(product -> product.getDeletedAt() == null)
                    .filter(product -> !"Inactive".equalsIgnoreCase(product.getProductStatus()))
                    .map(productMapper::toProductDetailResponse)
                    .toList();
        }
        return productRepository.findAll()
                .stream()
                .filter(product -> status.equalsIgnoreCase(product.getProductStatus()))
                .filter(product -> product.getDeletedAt() == null)
                .map(productMapper::toProductDetailResponse)
                .toList();
    }

    @Override
    public ProductDetailResponse updateStatusProduct(Long id, ProductRequest dto) {
        Product product = productRepository.findById(Integer.parseInt(id.toString()))
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setProductStatus(dto.getProductStatus());
        mailService.sendProductStatusEmailToSeller(product.getSeller().getUsers().getEmail(), product, dto.getText());
        productRepository.save(product);

        return productMapper.toProductDetailResponse(product);
    }

    public List<ProductInfoResponse> getAllProducts() {
        List<Product> products = productRepository.findAllByDeletedAtIsNullOrderByIdDesc();

        return products.stream().map(p -> {
            List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(p.getId());
            return ProductMapper.toResponse(p, mediaList);
        }).toList();
    }

    @Override
    public ByteArrayInputStream exportAllProductsToExcel(List<Product> products) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);

            String[] headers = {
                    "Shop Name",
                    "Category Name",
                    "Product Name",
                    "Weight",
                    "Description",
                    "Price",
                    "Status",
                    "Created At",
                    "Updated At"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            // Fill data rows (sắp xếp cell theo thứ tự)
            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getSeller().getShopName());
                row.createCell(1).setCellValue(p.getCategory().getName());
                row.createCell(2).setCellValue(p.getName());
                row.createCell(3).setCellValue(p.getWeight());
                row.createCell(4).setCellValue(p.getDescription());
                row.createCell(5).setCellValue(p.getPrice().doubleValue());
                row.createCell(6).setCellValue(p.getProductStatus());
                row.createCell(7).setCellValue(p.getCreatedAt().toString());
                row.createCell(8).setCellValue(p.getUpdatedAt().toString());
            }


            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error creating Excel file: " + e.getMessage());
        }
    }

    @Override
    public List<Product> getAllProductsWhereDeletedAtIsNull() {
        return productRepository.findAllByDeletedAtIsNullOrderByIdDesc();
    }

    public Page<ProductInfoResponse> getAllProductsRandomized(Pageable pageable) {

        Optional<Servicepackage> optionalServicePackage = servicePackageRepository.findByIdAndDeletedAtIsNull(2);
        Servicepackage servicePackage = optionalServicePackage.orElse(new Servicepackage());
        // 1. Lấy tất cả sản phẩm còn active (deletedAt IS NULL)
        List<Product> allProducts = productRepository.findAllByDeletedAtIsNull();

        // 2. Lấy danh sách productIds từ package usage của sellerpackage Active
        Optional<Servicepackage> optionalServicePackage1 = servicePackageRepository.findByIdAndDeletedAtIsNull(3);
        Servicepackage servicePackage1 = optionalServicePackage1.orElse(new Servicepackage());
        List<Sellerpackage> sellerPackage = sellerPackageRepository.findAllIdsByPackageFieldAndStatus(servicePackage1, "Active");
        List<PackageUsage> packageUsageList = pkgUsageRepository.findAllTargetIdBySellerPackageIn(sellerPackage);
        List<Integer> targetIds = packageUsageList.stream()
                .map(PackageUsage::getTargetId)
                .toList();
        List<Product> products = productRepository.findAllById(targetIds);

        // 3. Lấy danh sách productIds từ sellerpackage Active với packageId = 2
        Optional<Servicepackage> optionalServicePackage2 = servicePackageRepository.findByIdAndDeletedAtIsNull(2);
        Servicepackage servicePackage2 = optionalServicePackage2.orElse(new Servicepackage());
        List<Sellerpackage> sellerpackages = sellerPackageRepository.findAllSellersByPackageField(servicePackage2);
        List<Seller> sellers = sellerpackages.stream()
                .map(Sellerpackage::getSeller)
                .toList();
        List<Product> products1 = productRepository.findAllProductBySellerIn(sellers);

        // 4. Tạo các nhóm
        List<Product> group1 = allProducts.stream()
                .filter(p -> products.contains(p))
                .collect(Collectors.toList());

        List<Product> group2 = allProducts.stream()
                .filter(p -> products1.contains(p) && !products.contains(p))
                .collect(Collectors.toList());

        List<Product> group3 = allProducts.stream()
                .filter(p -> !products.contains(p) && !products1.contains(p))
                .collect(Collectors.toList());

        // 5. Shuffle từng nhóm
        Collections.shuffle(group1);
        Collections.shuffle(group2);
        Collections.shuffle(group3);

        // 6. Gộp lại
        List<Product> finalProducts = new ArrayList<>();
        finalProducts.addAll(group1);
        finalProducts.addAll(group2);
        finalProducts.addAll(group3);

        // 7. Tạo Page manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), finalProducts.size());

        List<ProductInfoResponse> pageContent = finalProducts.subList(start, end).stream()
                .map(p -> {
                    List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(p.getId());
                    return ProductMapper.toResponse(p, mediaList);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, finalProducts.size());
    }

    @Override
    public Page<ProductInfoResponse> getAllProductsOfShopHasServicePackage(Pageable pageable) {
        List<Product> allProducts = productRepository.findAllByDeletedAtIsNull();
        Optional<Servicepackage> optionalServicePackage = servicePackageRepository.findByIdAndDeletedAtIsNull(2);
        Servicepackage servicePackage = optionalServicePackage.orElse(new Servicepackage());
        List<Sellerpackage> sellerpackages = sellerPackageRepository.findAllSellersByPackageField(servicePackage);
        List<Seller> sellers = sellerpackages.stream()
                .map(Sellerpackage::getSeller)
                .toList();
        List<Product> products = productRepository.findAllProductBySellerIn(sellers);
        List<Product> productList = allProducts.stream()
                .filter(p -> products.contains(p))
                .collect(Collectors.toList());
        Collections.shuffle(productList);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), productList.size());

        List<ProductInfoResponse> pageContent = productList.subList(start, end).stream()
                .map(p -> {
                    List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(p.getId());
                    return ProductMapper.toResponse(p, mediaList);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, productList.size());
    }

    @Override
    public List<ProductInfoResponse> getProductsBySellerId(Integer sellerId) {
        List<Product> products = productRepository.findBySellerIdAndDeletedAtIsNullOrderByIdDesc(sellerId);

        return products.stream().map(p -> {
            List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(p.getId());
            return ProductMapper.toResponse(p, mediaList);
        }).toList();
    }

    @Override
    public List<ProductInfoResponse> getPromotedProducts() {
        // 1. Get active "Gói Quảng Cáo Sản Phẩm" packages (ID 3)
        Optional<Servicepackage> optionalServicePackage = servicePackageRepository.findByIdAndDeletedAtIsNull(3);
        if (optionalServicePackage.isEmpty()) {
            return new ArrayList<>();
        }
        Servicepackage servicePackage = optionalServicePackage.get();

        // 2. Find all active seller packages of this type
        List<Sellerpackage> activeSellerPackages = sellerPackageRepository.findAllIdsByPackageFieldAndStatus(servicePackage, "Active");


        if (activeSellerPackages.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. Get all product IDs from usage records of these packages
        List<PackageUsage> usages = pkgUsageRepository.findAllTargetIdBySellerPackageIn(activeSellerPackages);

        
        List<Integer> productIds = usages.stream()
                .filter(u -> "Product".equalsIgnoreCase(u.getTargetType()))
                .map(PackageUsage::getTargetId)
                .distinct()
                .toList();


        if (productIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. Fetch products
        List<Product> products = productRepository.findAllById(productIds);


        // 5. Randomize order
        Collections.shuffle(products);

        // 6. Map to response
        return products.stream()
                .filter(p -> p.getDeletedAt() == null && "Approved".equals(p.getProductStatus()))
                .map(p -> {
                    List<Productmedia> mediaList = productMediaRepository.findByProductIdAndDeletedAtIsNull(p.getId());
                    return ProductMapper.toResponse(p, mediaList);
                })
                .toList();
    }
}
