package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.template.contracts.ItemContract;
import com.template.states.ItemState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class AddItem extends FlowLogic<Void> {

    private final UUID key;
    private final String productName;
    private final String productDetails;
    private final double price;
    private final String expiryDate;
    private final int quantity;
    private final String barCode;
    private final String shopAccountName;

    public AddItem(UUID key, String productName, String productDetails, double price, String expiryDate, int quantity, String barCode, String shopAccountName) {
        this.key = key;
        this.productName = productName;
        this.productDetails = productDetails;
        this.price = price;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.barCode = barCode;
        this.shopAccountName = shopAccountName;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        UniqueIdentifier linearId = new UniqueIdentifier(null,key);
        UniqueIdentifier productId = new UniqueIdentifier(barCode);

        AccountInfo shopAccount = UtilitiesKt.getAccountService(this).accountInfo(shopAccountName).get(0).getState().getData();
        AnonymousParty shopParty = subFlow(new RequestKeyForAccount(shopAccount));

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        ItemState outputState = new ItemState(linearId,productId,productName, expiryDate, quantity, productDetails, price, shopAccountName, shopParty);
        Command command = new Command(new ItemContract.Generate(),shopParty.getOwningKey());

        TransactionBuilder txB = new TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(command);

        SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,shopParty.getOwningKey());

        SignedTransaction stx = subFlow(new FinalityFlow(selfSignedTx, Arrays.asList()));

        return null;
    }
}
