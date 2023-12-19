package com.template.webserver.models;

public class InventoryModel {
    private final String productKey;
    private final String productName;
    private final String productDetails;
    private final String expiryDate;
    private final int quantity;
    private final double price;
    private final String addedDate;

    public InventoryModel(String productKey, String productName, String productDetails, String expiryDate, int quantity, double price, String addedDate) {
        this.productKey = productKey;
        this.productName = productName;
        this.productDetails = productDetails;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.price = price;
        this.addedDate = addedDate;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getAddedDate() {
        return addedDate;
    }
}
