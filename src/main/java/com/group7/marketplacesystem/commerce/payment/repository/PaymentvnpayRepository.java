package com.group7.marketplacesystem.commerce.payment.repository;

import com.group7.marketplacesystem.commerce.payment.entity.Paymentvnpay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentvnpayRepository extends JpaRepository<Paymentvnpay, Integer> {
    
    Optional<Paymentvnpay> findTopByVnpTxnRefOrderByCreatedAtDesc(String txnRef);
}
