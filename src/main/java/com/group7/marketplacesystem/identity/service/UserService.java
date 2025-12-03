package com.group7.marketplacesystem.identity.service;


import com.group7.marketplacesystem.identity.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import com.group7.marketplacesystem.identity.dto.response.UserDetailResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<UserResponse> searchUsers(String username, String email, String phone, String role, String userStatus, Pageable pageable);

    UserDetailResponse getUserDetails(Integer userId);

    UserResponse updateUserStatus(Integer userId, String status);


    void deleteUser(Integer id);

    List<UserResponse> searchUsersByName(String searchTerm);
}
