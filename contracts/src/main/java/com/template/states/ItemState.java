package com.template.states;

import com.template.contracts.ItemContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ItemContract.class)
public class ItemState implements LinearState {

    //Product Key used to uniquely identify the item in inventory
    private final UniqueIdentifier linearId;

    //Product Details
    private final UniqueIdentifier productId;
    private final String productName;
    private final String expiryDate;
    private final int quantity;
    private final String productDetails;
    private final double price;
    private final String shopAccountName;

    //Product owning party
    private final AnonymousParty owner;

    public ItemState(UniqueIdentifier linearId, UniqueIdentifier productId, String productName, String expiryDate, int quantity, String productDetails, double price, String shopAccountName, AnonymousParty owner) {
        this.linearId = linearId;
        this.productId = productId;
        this.productName = productName;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.productDetails = productDetails;
        this.price = price;

        this.shopAccountName = shopAccountName;
        this.owner = owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public UniqueIdentifier getProductId() {
        return productId;
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

    public String getShopAccountName() {
        return shopAccountName;
    }

    public AnonymousParty getOwner() {
        return owner;
    }
}