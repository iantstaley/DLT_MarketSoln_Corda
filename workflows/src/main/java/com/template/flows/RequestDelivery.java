package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareStateAndSyncAccounts;
import com.template.contracts.RequestDeliveryContract;
import com.template.states.ItemState;
import com.template.states.OrderState;
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
import java.util.List;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class RequestDelivery extends FlowLogic<String> {

    private final UUID trackingId;

    private final String sender;
    private final String receiver;

    public RequestDelivery(UUID trackingId, String sender, String receiver) {
        this.trackingId = trackingId;
        this.sender = sender;
        this.receiver = receiver;
    }

    private StateAndRef<OrderState> findAssociatedOrderState(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<OrderState> orderState = getServiceHub().getVaultService().queryBy(OrderState.class,criteria).getStates().get(0);
        return orderState;
    }

    private StateAndRef<ItemState> findAssociatedItemState(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<ItemState> itemState = getServiceHub().getVaultService().queryBy(ItemState.class,criteria).getStates().get(0);
        return itemState;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.
        UniqueIdentifier linearId = new UniqueIdentifier(null,trackingId);

        AccountInfo senderAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(sender).get(0).getState().getData();
        AnonymousParty senderParty = subFlow(new RequestKeyForAccount(senderAccountInfo));
        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

        FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());

        StateAndRef<OrderState> associatedOrder = findAssociatedOrderState(linearId);
        StateAndRef<ItemState> associatedItem = findAssociatedItemState(associatedOrder.getState().getData().getProductKey());
        RequestDeliveryState outputState = new RequestDeliveryState(linearId,
                associatedItem.getState().getData().getLinearId().getId(),
                associatedItem.getState().getData().getProductId(),
                associatedItem.getState().getData().getProductId().getId(),
                associatedOrder.getState().getData().getShopAccountName(),
                associatedOrder.getState().getData().getUserAccountName(),
                associatedOrder.getState().getData().getDeliveryAddress(),
                senderParty,receiverParty);

        Command command = new Command(new RequestDeliveryContract.Issue(), Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(command);

        txB.verify(getServiceHub());

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,senderParty.getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession), Collections.singleton(senderParty.getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        //Searching the created output state and calling ShareStateAndSyncAccounts flow
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        List<StateAndRef<RequestDeliveryState>> requestDeliveryStates = getServiceHub().getVaultService().queryBy(RequestDeliveryState.class,criteria).getStates();
        subFlow(new ShareStateAndSyncAccounts(requestDeliveryStates.get(0),receiverAccountInfo.getHost()));

        return "Request send with id " + stx.getId();
    }
}

@InitiatedBy(RequestDelivery.class)
class RequestDeliveryResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public RequestDeliveryResponder(FlowSession counterpartySession) {
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