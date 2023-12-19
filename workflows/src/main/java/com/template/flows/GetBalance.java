package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.template.states.CoinState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class GetBalance extends FlowLogic<String> {

    private final String accountName;

    public GetBalance(String accountName) {
        this.accountName = accountName;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        return Double.toString(returnBalance());

    }

    public double returnBalance() {
        AccountInfo accountInfo = UtilitiesKt.getAccountService(this).accountInfo(accountName).get(0).getState().getData();
        UUID id = accountInfo.getIdentifier().getId();

        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria().withExternalIds(Arrays.asList(id));

        List<StateAndRef<CoinState>> coinStates = getServiceHub().getVaultService().queryBy(CoinState.class,criteria).getStates();

        if(coinStates.size() > 0)
            return coinStates.get(0).getState().getData().getValue();
        else
            return 0;
    }
}
