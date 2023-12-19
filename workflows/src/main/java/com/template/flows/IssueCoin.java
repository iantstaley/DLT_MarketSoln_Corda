package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.UtilitiesKt;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareStateAndSyncAccounts;
import com.template.contracts.CoinContract;
import com.template.states.CoinState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
@SchedulableFlow
public class IssueCoin extends FlowLogic<Void> {

    private final String sender;
    private final String receiver;
    private final double value;

    public IssueCoin(String sender, String receiver, double value) {
        this.sender = sender;
        this.receiver = receiver;
        this.value = value;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.
        if(getOurIdentity().getName().getOrganisation().equalsIgnoreCase("Bank")) {

            AccountInfo senderAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(sender).get(0).getState().getData();
            AccountInfo receiverAccountInfo = UtilitiesKt.getAccountService(this).accountInfo(receiver).get(0).getState().getData();

            AnonymousParty senderParty = subFlow(new RequestKeyForAccount(senderAccountInfo));
            AnonymousParty receiverParty = subFlow(new RequestKeyForAccount(receiverAccountInfo));

            FlowSession receiverSession = initiateFlow(receiverAccountInfo.getHost());

            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            Command command = new Command(new CoinContract.Issue(),Arrays.asList(senderParty.getOwningKey(),receiverParty.getOwningKey()));

            UUID id1 = receiverAccountInfo.getIdentifier().getId();
            UUID id2 = senderAccountInfo.getIdentifier().getId();
            QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria().withExternalIds(Arrays.asList(id1,id2)); //Find state which have either of sender or receiver as it's external id

            List<StateAndRef<CoinState>> allCoinStates = getServiceHub().getVaultService().queryBy(CoinState.class,criteria).getStates();
            List<StateAndRef<CoinState>> coinStates = new ArrayList<>();
            allCoinStates.forEach(it -> {
                if(it.getState().getData().getReceiverAccount().equals(receiver))
                    coinStates.add(it);
            });

            TransactionBuilder txB = new TransactionBuilder(notary)
                    .addCommand(command);

            double newValue = value;

            if(coinStates.size() > 0) {
                newValue += coinStates.stream().mapToDouble(it -> it.getState().getData().getValue()).sum();
                coinStates.stream().forEach(it -> txB.addInputState(it));
            }

            CoinState outputState = new CoinState(newValue,sender,senderParty,receiver,receiverParty);
            txB.addOutputState(outputState);

            SignedTransaction selfSignedTx = getServiceHub().signInitialTransaction(txB,senderParty.getOwningKey());
            final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(selfSignedTx,
                    Arrays.asList(receiverSession), Collections.singleton(senderParty.getOwningKey())));

            SignedTransaction stx = subFlow(new FinalityFlow(fullySignedTx,Arrays.asList(receiverSession)));


            //Searching the created state and calling ShareStateAndSyncAccounts flow

            List<StateAndRef<CoinState>> allCoinStateToShare = getServiceHub().getVaultService().queryBy(CoinState.class,criteria).getStates();
            List<StateAndRef<CoinState>> coinStatesToShare = new ArrayList<>();
            allCoinStateToShare.forEach(it -> {
                if(it.getState().getData().getReceiverAccount().equals(receiver))
                    coinStatesToShare.add(it);
            });
//            subFlow(new ShareStateAndSyncAccounts(coinStateToShare,receiverAccountInfo.getHost()));
            subFlow(new ShareStateAndSyncAccounts(coinStatesToShare.get(0),receiverAccountInfo.getHost()));

        } else {
            throw new FlowException("Only Bank can issue Coins");
        }
        return null;
    }
}

@InitiatedBy(IssueCoin.class)
class IssueCoinResponder extends FlowLogic<String> {

    private FlowSession counterpartySession;

    public IssueCoinResponder(FlowSession counterpartySession) {
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

