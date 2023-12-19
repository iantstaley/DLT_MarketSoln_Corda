package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryGuyModel {
    private final String deliveryPersonName;

    public DeliveryGuyModel(@JsonProperty("deliveryPersonName") String deliveryPersonName) {
        this.deliveryPersonName = deliveryPersonName;
    }

    public String getDeliveryPersonName() {
        return deliveryPersonName;
    }
}
