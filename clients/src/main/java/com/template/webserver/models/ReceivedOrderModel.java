package com.template.webserver.models;

public class ReceivedOrderModel {

    private final String trackingId;
    private final String productKey;
    private final String buyerAccountName;


    public ReceivedOrderModel(String trackingId, String productKey, String buyerAccountName) {
        this.trackingId = trackingId;
        this.productKey = productKey;
        this.buyerAccountName = buyerAccountName;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }
}
