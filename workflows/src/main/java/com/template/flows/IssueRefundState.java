package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.template.contracts.RefundContract;
import com.template.states.RefundState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
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
public class IssueRefundState extends FlowLogic<Void> {

    private final UUID productKey;
    private final String receiver;
    private final String sender;

    public IssueRefundState(UUID productKey, String sender, String receiver) {
        this.productKey = productKey;
        this.receiver = receiver;
        this.sender = sender;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        UniqueIdentifier linearId = new UniqueIdentifier(null,productKey);
        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

        AccountInfo senderAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(sender).get(0).getState().getData();
        AnonymousParty senderParty = subFlow(new RequestKeyForAccount(senderAccountInfo));

        FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        RefundState outputState = new RefundState(linearId,receiverParty);

        Command command = new Command(new RefundContract.Issue(), Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));

        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(command);

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,senderParty.getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession), Collections.singleton(senderParty.getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        return null;
    }
}

@InitiatedBy(IssueRefundState.class)
class IssueRefundStateResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public IssueRefundStateResponder(FlowSession counterpartySession) {
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
