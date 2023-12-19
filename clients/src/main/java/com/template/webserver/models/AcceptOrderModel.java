package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptOrderModel {

    private final String trackingId;
    private final String shopName;

    public AcceptOrderModel(@JsonProperty("trackingId") String trackingId,
                            @JsonProperty("shopName") String shopName) {
        this.trackingId = trackingId;
        this.shopName = shopName;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getShopName() {
        return shopName;
    }
}
