package com.template.webserver.models;

public class ReceivedDeliveryRequestModel {

    private final String orderId;
    private final String shopAccountName;
    private final String buyerAccountName;
    private final String deliveryAddress;

    public ReceivedDeliveryRequestModel(String orderId, String shopAccountName, String buyerAccountName, String deliveryAddress) {
        this.orderId = orderId;
        this.shopAccountName = shopAccountName;
        this.buyerAccountName = buyerAccountName;
        this.deliveryAddress = deliveryAddress;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }
}
