package com.group7.marketplacesystem.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductMediaRequest {
        @NotBlank
        private String url;

        @NotNull
        private String type; // "IMAGE" or "VIDEO"

        @NotBlank
        private String publicId;

        private Integer position = 0;

        // getters & setters

}
