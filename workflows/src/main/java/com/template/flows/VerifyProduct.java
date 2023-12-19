package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.ItemState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.Collections;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class VerifyProduct extends FlowLogic<String> {

    private final UUID productKey;
    private final String barCode;

    public VerifyProduct(UUID productKey, String barCode) {
        this.productKey = productKey;
        this.barCode = barCode;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(new UniqueIdentifier(null,productKey)),
                Vault.StateStatus.UNCONSUMED,
                null
        );

        StateAndRef<ItemState> itemStateStateAndRef = getServiceHub().getVaultService().queryBy(ItemState.class,criteria).getStates().get(0);

        if(itemStateStateAndRef.getState().getData().getProductId().getExternalId().equals(barCode)) {
            subFlow(new IssueVerificationState(productKey,"Bank"));
            return "Success";
        } else {
         return "Failed";
        }
    }
}
