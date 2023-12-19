package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FinalHandoverModel {

    private final String trackingId;
    private final String deliveryAccountName;

    public FinalHandoverModel(@JsonProperty("trackingId") String trackingId,
                              @JsonProperty("deliveryAccountName") String deliveryAccountName) {
        this.trackingId = trackingId;
        this.deliveryAccountName = deliveryAccountName;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getDeliveryAccountName() {
        return deliveryAccountName;
    }
}
