package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.template.contracts.VerificationContract;
import com.template.states.DeliveredState;
import com.template.states.VerificationState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
@SchedulableFlow
public class IssueVerificationState extends FlowLogic<Void> {

    private final UUID productKey;
    private final String receiver;

    public IssueVerificationState(UUID productKey, String receiver) {
        this.productKey = productKey;
        this.receiver = receiver;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        UniqueIdentifier linearId = new UniqueIdentifier(null,productKey);
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );

        StateAndRef<DeliveredState> deliveredStateStateAndRef = getServiceHub().getVaultService().queryBy(DeliveredState.class,criteria).getStates().get(0);

        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));
        FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        VerificationState outputState = new VerificationState(linearId,
                deliveredStateStateAndRef.getState().getData().getShopAccountName(),
                deliveredStateStateAndRef.getState().getData().getDeliveryAccountName(),
                receiverParty);
        Command command = new Command(new VerificationContract.Issue(), Arrays.asList(getOurIdentity().getOwningKey(),receiverParty.getOwningKey()));

        TransactionBuilder txB = new TransactionBuilder(notary)
                .addInputState(deliveredStateStateAndRef)
                .addOutputState(outputState)
                .addCommand(command);

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,getOurIdentity().getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession),Collections.singleton(getOurIdentity().getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        return null;
    }
}

@InitiatedBy(IssueVerificationState.class)
class IssueVerificationStateResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public IssueVerificationStateResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        subFlow(new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

            }
        });
        return subFlow(new ReceiveFinalityFlow(counterpartySession)).toString();
    }
}

