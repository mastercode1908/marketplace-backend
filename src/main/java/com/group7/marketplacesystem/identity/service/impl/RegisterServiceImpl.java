package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.cart.service.CartService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.JwtUtils;
import com.group7.marketplacesystem.communication.service.EmailTokenService;
import com.group7.marketplacesystem.identity.dto.request.UserRegisterRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.entity.UserProvider;
import com.group7.marketplacesystem.identity.mapper.AuthMapper;
import com.group7.marketplacesystem.identity.mapper.BuyerMapper;
import com.group7.marketplacesystem.identity.mapper.LocalUserMapper;
import com.group7.marketplacesystem.identity.mapper.SellerMapper;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserProviderRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.RegisterService;
import com.group7.marketplacesystem.identity.validator.PasswordValidator;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final BuyerMapper buyerMapper;
    private final SellerMapper sellerMapper;
    private final LocalUserMapper localUserMapper;
    private final EmailTokenService emailTokenService;
    private final UserProviderRepository userProviderRepository;
    private final CartService cartService;

    @Override
    public AuthResponse registerUser(UserRegisterRequest request) {
        // Kiểm tra email/username trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.MAIL_EXISTED);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(ErrorCode.USER_EXISTED);
        }

        // Validate password nâng cao ở service
        PasswordValidator.validate(request.getPassword());

        if (request.getRole().equalsIgnoreCase("BUYER")) {
            // Map sang entity User
            User user = buyerMapper.toUserEntity(request);

            //  Chỉ encode 1 lần duy nhất ở đây
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);

            // Map sang entity Buyer
            Buyer buyer = buyerMapper.toBuyerEntity(user);
            buyerRepository.save(buyer);

            UserProvider userProvider = localUserMapper.toUserProvider(user);
            userProviderRepository.save(userProvider);

            // Tạo cart cho BUYER
            cartService.createCart(user.getId());

            emailTokenService.createAndSendVerificationToken(user.getId(), user.getEmail());

        } else if (request.getRole().equalsIgnoreCase("SELLER")) {
            // Map sang entity User
            User user = sellerMapper.toUserEntity(request);

            // Chỉ encode 1 lần duy nhất ở đây
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);

            // Map sang entity Seller
            Seller seller = sellerMapper.toSellerEntity(user);
            sellerRepository.save(seller);

            UserProvider userProvider = localUserMapper.toUserProvider(user);
            userProviderRepository.save(userProvider);
            emailTokenService.createAndSendVerificationToken(user.getId(), user.getEmail());
        } else {
            throw new ApiException(ErrorCode.INVALID_ROLE);
        }


        // KHÔNG sinh JWT token ở đây
        return null; // hoặc trả về response đơn giản kiểu: "Pending verification"
    }


}
