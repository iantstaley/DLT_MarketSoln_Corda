package com.template.states;

import com.template.contracts.HandOverRequestContract;
import com.template.contracts.TemplateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
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
@BelongsToContract(HandOverRequestContract.class)
public class HandOverRequestState implements LinearState {

    private final UniqueIdentifier linearId;
    private final UUID productKey;
    private final String shopAccountName;
    private final String deliveryGuyAccountName;
    private final String buyerAccountName;
    private final String deliveryAddress;

    private final AnonymousParty owner;


    public HandOverRequestState(UniqueIdentifier linearId, UUID productKey, String shopAccountName, String deliveryGuyAccountName, String buyerAccountName, String deliveryAddress, AnonymousParty owner) {
        this.linearId = linearId;
        this.productKey = productKey;
        this.shopAccountName = shopAccountName;
        this.deliveryGuyAccountName = deliveryGuyAccountName;
        this.buyerAccountName = buyerAccountName;
        this.deliveryAddress = deliveryAddress;
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

    public UUID getProductKey() {
        return productKey;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }

    public String getDeliveryGuyAccountName() {
        return deliveryGuyAccountName;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public AnonymousParty getOwner() {
        return owner;
    }
}