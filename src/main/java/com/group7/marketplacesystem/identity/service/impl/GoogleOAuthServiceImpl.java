package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.commerce.cart.service.CartService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.common.security.JwtUtils;
import com.group7.marketplacesystem.identity.dto.response.*;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.entity.UserProvider;
import com.group7.marketplacesystem.identity.mapper.AuthMapper;
import com.group7.marketplacesystem.identity.mapper.BuyerMapper;
import com.group7.marketplacesystem.identity.mapper.GoogleUserMapper;
import com.group7.marketplacesystem.identity.mapper.SellerMapper;
import com.group7.marketplacesystem.identity.repository.*;
import com.group7.marketplacesystem.identity.service.IGoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements IGoogleOAuthService {

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;
    private final JwtUtils jwtUtils;
    private final RolePermissionRepository rolePermissionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthMapper authMapper;
    private final BuyerMapper buyerMapper;
    private final SellerMapper sellerMapper;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final CartService cartService;

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Value("${google.token.uri}")
    private String tokenUri;

    @Value("${google.userinfo.uri}")
    private String userInfoUri;

    @Override
    public String buildGoogleAuthUrl(String role) {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&state=" + role; // truyền role qua state (chuẩn OAuth2)
    }

    @Override
    public AuthResponse handleGoogleCallback(String code, @Nullable String role) {
        // Lấy access token từ Google
        String accessToken = getAccessToken(code);
        // Lấy thông tin người dùng từ Google
        GoogleUserInfoResponse userInfo = getUserInfo(accessToken);
        // Lưu hoặc cập nhật user vào DB
        User user;
        if (role == null || role.isEmpty()) {
            // Login flow: chỉ upsert login, không cần role
            user = upsertUserLogin(userInfo);
        } else {
            // Register flow: dùng role để tạo user mới nếu chưa có
            user = upsertUser(userInfo, role);
        }

        validateUserStatus(user);

        // Bọc user vào CustomUserDetails để tương thích với Spring Security co the sd
        // @PreAuthorize
        List<String> permissions = rolePermissionRepository.findPermissionCodesByRole(user.getRole().toUpperCase());
        CustomUserDetails userDetails = new CustomUserDetails(user, permissions);

        // Sinh JWT access & refresh token
        String jwtAccess = jwtUtils.generateAccessToken(userDetails);
        String jwtRefresh = jwtUtils.generateRefreshToken(userDetails);

        return authMapper.toAuthResponse(user, jwtAccess, jwtRefresh);
    }

    @Override
    public String buildGoogleAuthUrlLogin() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline";
    }

    private String getAccessToken(String code) {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> entity = new HttpEntity<>(params, headers);
        ResponseEntity<GoogleTokenResponse> res = restTemplate.exchange(tokenUri, HttpMethod.POST, entity,
                GoogleTokenResponse.class);

        return res.getBody().getAccess_token();
    }

    private GoogleUserInfoResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<GoogleUserInfoResponse> res = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity,
                GoogleUserInfoResponse.class);

        return res.getBody();
    }

    private User upsertUser(GoogleUserInfoResponse info, String role) {
        // Kiểm tra xem provider Google với sub này đã tồn tại chưa
        Optional<UserProvider> existingProvider = userProviderRepository
                .findByProviderAndProviderId("GOOGLE", info.getSub());

        if (existingProvider.isPresent()) {
            // Provider đã tồn tại → email này đã đăng ký Google rồi
            throw new ApiException(ErrorCode.MAIL_EXISTED);
        }

        // Kiểm tra xem email đã có user chưa
        User user = userRepository.findByEmail(info.getEmail())
                .map(u -> {
                    // Nếu role khác → chặn
                    if (!u.getRole().equalsIgnoreCase(role)) {
                        throw new ApiException(ErrorCode.INVALID_ROLE);
                    }
                    // Nếu email chưa verify → set và save
                    if (!Boolean.TRUE.equals(u.getEmailVerified())) {
                        u.setEmailVerified(true);
                        userRepository.save(u);
                    }
                    return u;
                })
                .orElseGet(() -> {
                    // Nếu chưa có user → tạo mới
                    User newUser = GoogleUserMapper.toUser(info);
                    newUser.setRole(role);
                    newUser.setUsername(info.getEmail().split("@")[0]);
                    newUser.setEmailVerified(true);
                    if (role.equalsIgnoreCase("BUYER")) {
                        newUser.setUserStatus("Active");
                        Buyer buyer = buyerMapper.toBuyerEntity(newUser);
                        buyerRepository.save(buyer);

                        cartService.createCart(newUser.getId());
                    } else {
                        newUser.setUserStatus("Incomplete");
                        Seller seller = sellerMapper.toSellerEntity(newUser);
                        sellerRepository.save(seller);
                        // Chuyen huong code o day sang trang bo sung thong tin cho seller
                    }
                    newUser.setCreatedAt(Instant.now());
                    newUser.setUpdatedAt(Instant.now());
                    return userRepository.save(newUser);
                });

        // Tạo mới liên kết Google account (đã check provider không tồn tại ở trên)
        UserProvider userProvider = GoogleUserMapper.toUserProvider(user, info);
        userProviderRepository.save(userProvider);

        // Trả về user đã upsert
        return user;
    }

    private User upsertUserLogin(GoogleUserInfoResponse info) {
        // Kiểm tra xem email đã có user chưa
        User user = userRepository.findByEmail(info.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // Nếu email chưa verify → set và save
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        // Kiểm tra UserProvider theo provider = GOOGLE + sub
        boolean providerExists = userProviderRepository
                .findByProviderAndProviderId("GOOGLE", info.getSub())
                .isPresent();

        if (!providerExists) {
            // Liên kết Google account nếu chưa có
            UserProvider userProvider = GoogleUserMapper.toUserProvider(user, info);
            userProviderRepository.save(userProvider);
        }

        // Trả về user đã login
        return user;
    }

    private void validateUserStatus(User user) {
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED);
        }

        switch (user.getUserStatus()) {
            case "Banned" -> throw new ApiException(ErrorCode.ACCOUNT_BANNED);
            case "Inactive" -> throw new ApiException(ErrorCode.ACCOUNT_INACTIVE);
            case "Pending" -> throw new ApiException(ErrorCode.ACCOUNT_PENDING);
            case "Reviewing" -> throw new ApiException(ErrorCode.ACCOUNT_REVIEWING);
        }
    }
}
