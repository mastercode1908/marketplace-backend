package com.group7.marketplacesystem.commerce.payment.mapper;

import com.group7.marketplacesystem.commerce.payment.entity.Paymentvnpay;
import com.group7.marketplacesystem.commerce.payment.response.PaymentVnpayResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentVnpayMapper {

    public PaymentVnpayResponse toResponse(Paymentvnpay entity) {
        if (entity == null) return null;

        PaymentVnpayResponse res = new PaymentVnpayResponse();
        res.setId(entity.getId());
        res.setVnpTransactionCode(entity.getVnpTransactionCode());
        res.setVnpResponseCode(entity.getVnpResponseCode());
        res.setVnpBankCode(entity.getVnpBankCode());
        res.setVnpPayDate(entity.getVnpPayDate());
        res.setVnpOrderInfo(entity.getVnpOrderInfo());
        res.setStatus(entity.getStatus());
        res.setCreatedAt(entity.getCreatedAt());
        res.setAmount(entity.getAmount());
        res.setVnpTxnRef(entity.getVnpTxnRef());
        return res;
    }
}
