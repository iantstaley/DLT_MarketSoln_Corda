package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemModel {

    private final String productName;
    private final String productDetails;
    private final double price;
    private final String expiryDate;
    private final int quantity;
    private final String barCode;
    private final String shopAccountName;

    public ItemModel(@JsonProperty("productName") String productName,
                     @JsonProperty("productDetails") String productDetails,
                     @JsonProperty("price") double price,
                     @JsonProperty("expiryDate") String expiryDate,
                     @JsonProperty("quantity") int quantity,
                     @JsonProperty("barCode") String barCode,
                     @JsonProperty("shopAccountName") String shopAccountName) {
        this.productName = productName;
        this.productDetails = productDetails;
        this.price = price;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.barCode = barCode;
        this.shopAccountName = shopAccountName;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public double getPrice() {
        return price;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getBarCode() {
        return barCode;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }
}
