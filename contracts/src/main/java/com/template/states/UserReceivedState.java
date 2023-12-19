package com.template.states;

import com.template.contracts.TemplateContract;
import com.template.contracts.UserReceivedContract;
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
@BelongsToContract(UserReceivedContract.class)
public class UserReceivedState implements LinearState {

    private final UniqueIdentifier linearId;
    private final String shopAccountName;
    private final String deliveryPersonAccountName;
    private final String buyerAccountName;

    private final AnonymousParty sender;
    private final AnonymousParty receiver;

    public UserReceivedState(UniqueIdentifier linearId, String shopAccountName, String deliveryPersonAccountName, String buyerAccountName, AnonymousParty sender, AnonymousParty receiver) {
        this.linearId = linearId;

        this.shopAccountName = shopAccountName;
        this.deliveryPersonAccountName = deliveryPersonAccountName;
        this.buyerAccountName = buyerAccountName;
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

    public String getShopAccountName() {
        return shopAccountName;
    }

    public String getDeliveryPersonAccountName() {
        return deliveryPersonAccountName;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }

    public AnonymousParty getSender() {
        return sender;
    }

    public AnonymousParty getReceiver() {
        return receiver;
    }
}