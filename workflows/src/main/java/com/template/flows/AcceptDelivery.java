package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareStateAndSyncAccounts;
import com.template.contracts.DeliveryRespondeContract;
import com.template.states.DeliveryRespondState;
import com.template.states.RequestDeliveryState;
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
public class AcceptDelivery extends FlowLogic<String> {

    private final UUID orderId;
    private final String barCode;

    private final String sender;
    private final String receiver;

    public AcceptDelivery(UUID orderId, String barCode, String sender, String receiver) {
        this.orderId = orderId;
        this.barCode = barCode;
        this.sender = sender;
        this.receiver = receiver;
    }

    private StateAndRef<RequestDeliveryState> findDeliveryRequest(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<RequestDeliveryState> request = getServiceHub().getVaultService().queryBy(RequestDeliveryState.class,criteria).getStates().get(0);
        return request;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        UniqueIdentifier linearId = new UniqueIdentifier(null, orderId);

        StateAndRef<RequestDeliveryState> requestState = findDeliveryRequest(linearId);
        UUID productKey = requestState.getState().getData().getProductKey();

        UniqueIdentifier expectedID = new UniqueIdentifier(barCode,requestState.getState().getData().getUid());
        if(requestState.getState().getData().getProductId().getExternalId().equals(barCode)) {

            AccountInfo deliveryPersonAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(sender).get(0).getState().getData();
            AccountInfo shopAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();

            AnonymousParty deliveryPersonParty = subFlow(new RequestKeyForAccount(deliveryPersonAccountInfo));
            AnonymousParty shopAccountParty = subFlow(new RequestKeyForAccount(shopAccountInfo));

            FlowSession receiverSession = initiateFlow(shopAccountParty);

            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            DeliveryRespondState outputState = new DeliveryRespondState(linearId, productKey, receiver,sender,deliveryPersonParty,shopAccountParty);
            Command command = new Command(new DeliveryRespondeContract.Generate(), Arrays.asList(deliveryPersonParty.getOwningKey(),shopAccountParty.getOwningKey()));

            TransactionBuilder txB = new TransactionBuilder(notary)
                    .addInputState(requestState)
                    .addOutputState(outputState)
                    .addCommand(command);

            txB.verify(getServiceHub());

            SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,deliveryPersonParty.getOwningKey());

            final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                    Arrays.asList(receiverSession),Collections.singleton(deliveryPersonParty.getOwningKey())));

            SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

            //Search for the created output state and ShareAndSyncAccount
            QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                    null,
                    Collections.singletonList(linearId),
                    Vault.StateStatus.UNCONSUMED,
                    null
            );
            StateAndRef<DeliveryRespondState> deliveryRespondStateStateAndRef = getServiceHub().getVaultService().queryBy(DeliveryRespondState.class,criteria).getStates().get(0);
            subFlow(new ShareStateAndSyncAccounts(deliveryRespondStateStateAndRef,shopAccountInfo.getHost()));
            return "Success";

        } else {
            return "Failed";
        }
    }
}

@InitiatedBy(AcceptDelivery.class)
class AcceptDeliveryResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public AcceptDeliveryResponder(FlowSession counterpartySession) {
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
