package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.dto.request.BuyerRequest;
import com.group7.marketplacesystem.identity.dto.request.BuyerUpdateRequest;
import com.group7.marketplacesystem.identity.dto.request.UserRequest;
import com.group7.marketplacesystem.identity.dto.response.BuyerResponse;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.mapper.BuyerMapper;
import com.group7.marketplacesystem.identity.mapper.UserMapper;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;
    private final BuyerMapper buyerMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public BuyerResponse getBuyerById(Integer buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));
        return  buyerMapper.toBuyerResponse(buyer);
    }

    @Override
    public List<BuyerResponse> getAllBuyer() {
        return List.of();
    }

    @Override
    public BuyerResponse updateBuyer(BuyerUpdateRequest dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName() ;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        //validate phone number
        if (dto.getUser().getPhone() == null || dto.getUser().getPhone().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_PHONE_NUMBER);
        }
        String phone = dto.getUser().getPhone();
        if (dto.getUser().getPhone().startsWith("+84")) {
            phone = "0" + dto.getUser().getPhone().substring(3);
        }
        String regex = "^(0[1-9][0-9]{8,9})$";
        if (!phone.trim().matches(regex)) {
            throw new ApiException(ErrorCode.INVALID_PHONE_NUMBER);
        }

        //validate fullname
        String fullName = dto.getUser().getFullName().trim();
        if (fullName.length() < 4) {
            throw new ApiException(ErrorCode.INVALID_FULLNAME);
        }
        String regex1 = "^[\\p{L}]+(\\s[\\p{L}]+)*$";
        if (!fullName.matches(regex1)) {
            throw new ApiException(ErrorCode.INVALID_FULLNAME_SYNTAX);
        }
        if (dto.getUser().getPhone() != null) {
            user.setPhone(phone);
        }
        if (dto.getUser().getFullName() != null) {
            user.setFullName(fullName);
        }
        if (dto.getUser().getGender() != null) {
            user.setGender(dto.getUser().getGender());
        }
        if (dto.getUser().getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getUser().getAvatarUrl());
        }
        if (dto.getBuyer().getAddress() != null) {
            buyer.setAddress(dto.getBuyer().getAddress());
        }
        if (dto.getBuyer().getDateOfBirth() != null) {
            buyer.setDateOfBirth(dto.getBuyer().getDateOfBirth());
        }


//        buyer.setDeletedAt(dto.getBuyer().getDeletedAt());

//        buyer.setDeletedAt(dto.getBuyer().getDeletedAt());


        // Lưu thay đổi
        userRepository.save(user);
        buyerRepository.save(buyer);

        return buyerMapper.toBuyerResponse(buyer);

    }

    @Override
    public Buyer getBuyerByBuyerId(Integer buyerId) {
        return buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
    }

    @Override
    public BuyerResponse getBuyer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName() ;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        return buyerMapper.toBuyerResponse(buyer);
    }
}
