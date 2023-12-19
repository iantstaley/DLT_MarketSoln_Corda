package com.template.states;

import com.template.contracts.RequestDeliveryContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(RequestDeliveryContract.class)
public class RequestDeliveryState implements LinearState {

    private final UniqueIdentifier linearId; //This is the order id
    private final UUID productKey;
    private final UniqueIdentifier productId;
    private final UUID uid;
    private final String shopAccountName;
    private final String buyerAccountName;
    private final String buyerAddress;

    private final AnonymousParty sender;
    private final AnonymousParty receiver;

    public RequestDeliveryState(UniqueIdentifier linearId, UUID productKey, UniqueIdentifier productId, UUID uid, String shopAccountName, String buyerAccountName, String buyerAddress, AnonymousParty sender, AnonymousParty receiver) {
        this.linearId = linearId;
        this.productKey = productKey;
        this.productId = productId;
        this.uid = uid;
        this.shopAccountName = shopAccountName;

        this.buyerAccountName = buyerAccountName;
        this.buyerAddress = buyerAddress;
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

    public UUID getUid() {
        return uid;
    }

    public UUID getProductKey() {
        return productKey;
    }

    public UniqueIdentifier getProductId() {
        return productId;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }

    public String getBuyerAddress() {
        return buyerAddress;
    }

    public AnonymousParty getSender() {
        return sender;
    }

    public AnonymousParty getReceiver() {
        return receiver;
    }
}