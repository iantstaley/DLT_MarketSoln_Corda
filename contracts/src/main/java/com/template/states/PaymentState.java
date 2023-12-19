package com.template.states;

import com.template.contracts.PaymentContract;
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
@BelongsToContract(PaymentContract.class)
public class PaymentState implements LinearState {

    private final UniqueIdentifier linearId;
    private final double amtToShop;
    private final double amtToDelivery;

    private final String senderAccountName;
    private final AnonymousParty issuer;
    private final AnonymousParty owner;

    public PaymentState(UniqueIdentifier linearId, double amtToShop, double amtToDelivery, String senderAccountName, AnonymousParty issuer, AnonymousParty owner) {
        this.linearId = linearId;
        this.amtToShop = amtToShop;
        this.amtToDelivery = amtToDelivery;
        this.senderAccountName = senderAccountName;
        this.issuer = issuer;

        this.owner = owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer,owner);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public double getAmtToShop() {
        return amtToShop;
    }

    public double getAmtToDelivery() {
        return amtToDelivery;
    }

    public String getSenderAccountName() {
        return senderAccountName;
    }

    public AnonymousParty getIssuer() {
        return issuer;
    }

    public AnonymousParty getOwner() {
        return owner;
    }
}