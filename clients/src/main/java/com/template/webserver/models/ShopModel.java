package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopModel {
    private final String shopName;

    public ShopModel(@JsonProperty("shopName") String shopName) {
        this.shopName = shopName;
    }

    public String getShopName() {
        return shopName;
    }
}
