package com.group7.marketplacesystem.communication.repository;

import com.group7.marketplacesystem.communication.entity.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailToken, Integer> {
    Optional<EmailToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM EmailToken e WHERE e.token = :token")
    void deleteByToken(@Param("token") String token);
}
