package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class CreateNewAccount extends FlowLogic<String> {

    private final String accountName;
    private final List<Party> shareWith;

    public CreateNewAccount(String accountName, List<Party> shareWith) {
        this.accountName = accountName;
        this.shareWith = shareWith;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.
        try {
            StateAndRef<AccountInfo> accountInfoStateAndRef = (StateAndRef<AccountInfo>) subFlow(new CreateAccount(accountName));
            subFlow(new ShareAccountInfo(accountInfoStateAndRef, shareWith));
            return accountInfoStateAndRef.getState().getData().getIdentifier().toString();
        } catch (Exception exp) {
//            return "Creation failed with error: " + exp.getMessage();
            return "Creation Failed";
        }
    }
}
