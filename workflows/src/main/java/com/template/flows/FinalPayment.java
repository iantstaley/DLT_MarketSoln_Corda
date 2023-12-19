package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.PaymentState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.Collections;
import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
@SchedulableFlow
public class FinalPayment extends FlowLogic<Void> {

    private final UniqueIdentifier linearId;
    private final String deliveryAccount;
    private final String shopAccount;

    public FinalPayment(UniqueIdentifier linearId, String shopAccount, String deliveryAccount) {
        this.linearId = linearId;
        this.deliveryAccount = deliveryAccount;
        this.shopAccount = shopAccount;
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
        double amtToShop = paymentState.getState().getData().getAmtToShop();
        double amtToDelivery = paymentState.getState().getData().getAmtToDelivery();

        subFlow(new IssueCoin("Bank",shopAccount,amtToShop));
        subFlow(new IssueCoin("Bank",deliveryAccount,amtToDelivery));

        return null;
    }
}
