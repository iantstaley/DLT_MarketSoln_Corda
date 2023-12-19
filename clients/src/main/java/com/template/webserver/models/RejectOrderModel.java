package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RejectOrderModel {
    private final String productKey;
    private final String accountName;

    public RejectOrderModel(@JsonProperty("productKey") String productKey,
                            @JsonProperty("accountName") String accountName) {
        this.productKey = productKey;
        this.accountName = accountName;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getAccountName() {
        return accountName;
    }
}
