package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareStateAndSyncAccounts;
import com.template.contracts.OrderContract;
import com.template.states.OrderState;
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
public class PlaceOrder extends FlowLogic<String> {

    private final UUID key;
    private final String buyerAccountName;
    private final String shopAccountName;
    private final String deliveryAddress;
    private final double amtToShop;
    private final double amtToDelivery;

    public PlaceOrder(UUID key, String buyerAccountName, String shopAccountName, String deliveryAddress, double amtToShop, double amtToDelivery) {
        this.key = key;
        this.buyerAccountName = buyerAccountName;
        this.shopAccountName = shopAccountName;
        this.deliveryAddress = deliveryAddress;
        this.amtToShop = amtToShop;
        this.amtToDelivery = amtToDelivery;
    }

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        AccountInfo buyerAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(buyerAccountName).get(0).getState().getData();
        AccountInfo shopAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(shopAccountName).get(0).getState().getData();

        AnonymousParty buyerParty = subFlow(new RequestKeyForAccount(buyerAccountInfo));
        AnonymousParty shopParty = subFlow(new RequestKeyForAccount(shopAccountInfo));

        FlowSession receiverSession = initiateFlow(shopAccountInfo.getHost());

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        UniqueIdentifier orderId = new UniqueIdentifier();
        UniqueIdentifier productKey = new UniqueIdentifier(null,key);

        OrderState outputState = new OrderState(orderId,productKey,buyerAccountName,shopAccountName,deliveryAddress,buyerParty,shopParty);
        Command command = new Command(new OrderContract.Generate(), Arrays.asList(buyerParty.getOwningKey(),shopParty.getOwningKey()));
        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(command);

        txB.verify(getServiceHub());

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,buyerParty.getOwningKey());
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(receiverSession), Collections.singleton(buyerParty.getOwningKey())));

        SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));

        //Searching the created output state and calling ShareStateAndSyncAccounts flow

        QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(orderId),
                Vault.StateStatus.UNCONSUMED,
                null
        );
        List<StateAndRef<OrderState>> orderStates = getServiceHub().getVaultService().queryBy(OrderState.class,criteria).getStates();
        subFlow(new ShareStateAndSyncAccounts(orderStates.get(0),shopAccountInfo.getHost()));

        subFlow(new MakePayment(key,buyerAccountName,"Bank",amtToShop,amtToDelivery));

        return orderId.getId().toString();
    }
}

@InitiatedBy(PlaceOrder.class)
class PlaceOrderResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public PlaceOrderResponder(FlowSession counterpartySession) {
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
