package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DummyMeta {
    private String createdAt;
    private String updatedAt;
    private String barcode;
    private String qrCode;
}
