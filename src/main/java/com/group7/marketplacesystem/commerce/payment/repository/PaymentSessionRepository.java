package com.group7.marketplacesystem.commerce.payment.repository;

import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentSessionRepository extends JpaRepository<PaymentSession, Long> {
    Optional<Object> findByTxnRef(String txnRef);
}