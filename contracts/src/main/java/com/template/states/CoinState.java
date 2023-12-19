package com.template.states;

import com.template.contracts.CoinContract;
import com.template.contracts.TemplateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(CoinContract.class)
public class CoinState implements ContractState {

    private final double value;
    private final String senderAccount;
    private final AnonymousParty sender;
    private final String receiverAccount;
    private final AnonymousParty owner;

    public CoinState(double value, String senderAccount, AnonymousParty sender, String receiverAccount, AnonymousParty owner) {
        this.value = value;
        this.senderAccount = senderAccount;
        this.sender = sender;
        this.receiverAccount = receiverAccount;
        this.owner = owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sender,owner);
    }

    public double getValue() {
        return value;
    }

    public AnonymousParty getSender() {
        return sender;
    }

    public AnonymousParty getOwner() {
        return owner;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }
}