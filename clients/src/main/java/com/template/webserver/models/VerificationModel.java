package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerificationModel {

    private final String productKey;
    private final String barCode;

    public VerificationModel(@JsonProperty("productKey") String productKey,
                             @JsonProperty("barCode") String barCode) {
        this.productKey = productKey;
        this.barCode = barCode;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getBarCode() {
        return barCode;
    }
}
