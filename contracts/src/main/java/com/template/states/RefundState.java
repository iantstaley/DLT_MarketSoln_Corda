package com.template.states;

import com.template.contracts.RefundContract;
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
@BelongsToContract(RefundContract.class)
public class RefundState implements SchedulableState, LinearState {

    private final UniqueIdentifier linearId;
    private final AnonymousParty owner;

    private final Instant nextActivityTime;

    public RefundState(UniqueIdentifier linearId, AnonymousParty owner) {
        this.linearId = linearId;
        this.owner = owner;
        this.nextActivityTime = Instant.now().plusSeconds(5);
    }

    @ConstructorForDeserialization
    public RefundState(UniqueIdentifier linearId, AnonymousParty owner, Instant nextActivityTime) {

        this.linearId = linearId;
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
        return new ScheduledActivity(flowLogicRefFactory.create("com.template.flows.Refund",linearId),nextActivityTime);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public AnonymousParty getOwner() {
        return owner;
    }
}