package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.template.contracts.HandOverRequestContract;
import com.template.contracts.ItemContract;
import com.template.states.HandOverRequestState;
import com.template.states.ItemState;
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
public class HandoverItem extends FlowLogic<String> {

    private final UUID trackingId;

    public HandoverItem(UUID trackingId) {
        this.trackingId = trackingId;
    }

    private final StateAndRef<HandOverRequestState> findHandOverRequest(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<HandOverRequestState> handOverRequestStateStateAndRef = getServiceHub().getVaultService().queryBy(HandOverRequestState.class,criteria).getStates().get(0);
        return handOverRequestStateStateAndRef;
    }

    private final StateAndRef<ItemState> findItemState(UniqueIdentifier linearId) {
        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(linearId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        StateAndRef<ItemState> itemStateStateAndRef = getServiceHub().getVaultService().queryBy(ItemState.class,criteria).getStates().get(0);
        return itemStateStateAndRef;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        UniqueIdentifier trackingLinearId = new UniqueIdentifier(null,trackingId);

        StateAndRef<HandOverRequestState> requestStateAndRef = findHandOverRequest(trackingLinearId);

        String receiver = requestStateAndRef.getState().getData().getBuyerAccountName();
        String sender = requestStateAndRef.getState().getData().getDeliveryGuyAccountName();

        AccountInfo senderAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(sender).get(0).getState().getData();
        AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();

        AnonymousParty senderParty = subFlow(new RequestKeyForAccount(senderAccountInfo));
        AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

        FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());

        UUID productKey = requestStateAndRef.getState().getData().getProductKey();
        UniqueIdentifier productKeyLinearId = new UniqueIdentifier(null,productKey);

        StateAndRef<ItemState> inputItem = findItemState(productKeyLinearId);

        ItemState outputItemState = new ItemState(productKeyLinearId,
                inputItem.getState().getData().getProductId(),
                inputItem.getState().getData().getProductName(),
                inputItem.getState().getData().getExpiryDate(),
                inputItem.getState().getData().getQuantity(),
                inputItem.getState().getData().getProductDetails(),
                inputItem.getState().getData().getPrice(),
                inputItem.getState().getData().getShopAccountName(),
                receiverParty);

        Command itemCommand = new Command(new ItemContract.Transfer(), Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));
        Command handoverCommand = new Command(new HandOverRequestContract.Consume(),Arrays.asList(senderParty.getOwningKey()));

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        TransactionBuilder txB = new TransactionBuilder(notary)
                .addInputState(requestStateAndRef)
                .addInputState(inputItem)
                .addOutputState(outputItemState)
                .addCommand(itemCommand)
                .addCommand(handoverCommand);

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,senderParty.getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession),Collections.singletonList(senderParty.getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        subFlow(new ConfirmDelivered(productKey,inputItem.getState().getData().getShopAccountName(),sender,receiver));

        return "Success with Id: " + stx.getId();
    }
}

@InitiatedBy(HandoverItem.class)
class HandoverItemResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public HandoverItemResponder(FlowSession counterpartySession) {
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
