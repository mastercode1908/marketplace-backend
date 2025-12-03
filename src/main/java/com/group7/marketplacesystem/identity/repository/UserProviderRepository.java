package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Integer> {
    Optional<UserProvider> findByProviderAndProviderId(String google, String sub);
}