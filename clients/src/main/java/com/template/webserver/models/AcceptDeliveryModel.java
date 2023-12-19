package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptDeliveryModel {

    private final String orderId;
    private final String barCode;
    private final String acceptor;
    private final String shopAccountName;

    public AcceptDeliveryModel(@JsonProperty("orderId") String orderId,
                               @JsonProperty("barCode") String barCode,
                               @JsonProperty("acceptor") String acceptor,
                               @JsonProperty("shopAccountName") String shopAccountName) {
        this.orderId = orderId;
        this.barCode = barCode;
        this.acceptor = acceptor;
        this.shopAccountName = shopAccountName;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBarCode() {
        return barCode;
    }

    public String getAcceptor() {
        return acceptor;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }
}
