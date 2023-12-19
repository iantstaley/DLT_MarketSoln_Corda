package com.template.states;

import com.template.contracts.VerificationContract;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(VerificationContract.class)
public class VerificationState implements SchedulableState, LinearState {

    private final UniqueIdentifier linearId;

    private final String shopAccountName;
    private final String deliveryAccountName;

    private final AnonymousParty owner;

    private final Instant nextActivityTime;

    public VerificationState(UniqueIdentifier linearId, String shopAccountName, String deliveryAccountName, AnonymousParty owner) {

        this.linearId = linearId;
        this.shopAccountName = shopAccountName;
        this.deliveryAccountName = deliveryAccountName;
        this.owner = owner;
        this.nextActivityTime = Instant.now().plusSeconds(10);
    }

    @ConstructorForDeserialization
    public VerificationState(UniqueIdentifier linearId, String shopAccountName, String deliveryAccountName, AnonymousParty owner, Instant nextActivityTime) {
        this.linearId = linearId;
        this.shopAccountName = shopAccountName;
        this.deliveryAccountName = deliveryAccountName;
        this.owner = owner;
        this.nextActivityTime = nextActivityTime;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }

    @Nullable
    @Override
    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {

        return new ScheduledActivity(flowLogicRefFactory.create("com.template.flows.FinalPayment",linearId,shopAccountName,deliveryAccountName),nextActivityTime);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }
}