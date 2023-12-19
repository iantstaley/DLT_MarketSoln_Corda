package com.template.states;

import com.template.contracts.TemplateContract;
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
@BelongsToContract(TemplateContract.class)
public class OrderState implements LinearState {

    private final UniqueIdentifier linearId;  //This is the order Id
    private final UniqueIdentifier productKey; //The key used to manage products in inventory
    private final String userAccountName;
    private final String shopAccountName;
    private final String deliveryAddress;

    private final AnonymousParty sender;
    private final AnonymousParty receiver;


    public OrderState(UniqueIdentifier linearId, UniqueIdentifier productKey, String userAccountName, String shopAccountName, String deliveryAddress, AnonymousParty sender, AnonymousParty receiver) {
        this.linearId = linearId;
        this.productKey = productKey;
        this.userAccountName = userAccountName;
        this.shopAccountName = shopAccountName;
        this.deliveryAddress = deliveryAddress;
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sender,receiver);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public UniqueIdentifier getProductKey() {
        return productKey;
    }

    public String getUserAccountName() {
        return userAccountName;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public AnonymousParty getSender() {
        return sender;
    }

    public AnonymousParty getReceiver() {
        return receiver;
    }
}