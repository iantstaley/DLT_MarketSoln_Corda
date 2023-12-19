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
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(TemplateContract.class)
public class DeliveryRespondState implements LinearState {

    private final UniqueIdentifier linearId;
    private final UUID productKey;
    private final String shopName;
    private final String carrierName;

    private final AnonymousParty sender;
    private final AnonymousParty receiver;

    public DeliveryRespondState(UniqueIdentifier linearId, UUID productKey, String shopName, String carrierName, AnonymousParty sender, AnonymousParty receiver) {

        this.linearId = linearId;
        this.productKey = productKey;
        this.shopName = shopName;
        this.carrierName = carrierName;
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

    public UUID getProductKey() {
        return productKey;
    }

    public String getShopName() {
        return shopName;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public AnonymousParty getSender() {
        return sender;
    }

    public AnonymousParty getReceiver() {
        return receiver;
    }
}