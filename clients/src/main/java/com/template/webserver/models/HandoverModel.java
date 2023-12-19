package com.template.webserver.models;

public class HandoverModel {
    private final String trackingId;
    private final String buyerAccountName;
    private final String deliveryAddress;

    public HandoverModel(String trackingId, String buyerAccountName, String deliveryAddress) {
        this.trackingId = trackingId;
        this.buyerAccountName = buyerAccountName;
        this.deliveryAddress = deliveryAddress;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }
}
