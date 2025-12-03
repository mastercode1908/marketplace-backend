package com.group7.marketplacesystem.common.config;

import com.group7.marketplacesystem.common.utils.VNPayUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class VNPayInitializer {

    private final VNPayConfig vnPayConfig;

    @PostConstruct
    public void init() {
        VNPayUtils.setConfig(vnPayConfig);
    }
}
