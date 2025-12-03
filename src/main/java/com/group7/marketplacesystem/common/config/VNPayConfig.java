package com.group7.marketplacesystem.common.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Getter
@Setter
public class VNPayConfig {

    @Value("${vnpay.tmnCode}")
    public String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    public String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    public String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    public String vnp_ReturnUrl;

    @Value("${vnpay.ipnUrl}")
    public String vnp_IpnUrl;

}
