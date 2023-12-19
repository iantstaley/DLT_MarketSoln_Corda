package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.PaymentState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.Collections;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
@SchedulableFlow
public class Refund extends FlowLogic<Void> {

    private final UniqueIdentifier linearId;

    public Refund(UniqueIdentifier linearId) {
        this.linearId = linearId;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<PaymentState> paymentState = getServiceHub().getVaultService().queryBy(PaymentState.class,criteria).getStates().get(0);
        String buyerAccountName = paymentState.getState().getData().getSenderAccountName();
        double amtToDelivery = paymentState.getState().getData().getAmtToDelivery();
        double amtToShop = paymentState.getState().getData().getAmtToShop();
        double totalAmt = amtToDelivery + amtToShop;

        subFlow(new IssueCoin("Bank",buyerAccountName,totalAmt));

        return null;
    }
}
