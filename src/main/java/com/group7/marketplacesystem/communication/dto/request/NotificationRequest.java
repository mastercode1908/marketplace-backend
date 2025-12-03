package com.group7.marketplacesystem.communication.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull(message = "Tiêu đề không được để trống.")
    private String title;

    @NotNull(message = "Nội dung không được để trống.")
    private String message;

    @Builder.Default
    private String type = "System";

}
