package com.group7.marketplacesystem.communication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateConversationRequest {
    private Integer buyerId;
    private Integer sellerId;
}
