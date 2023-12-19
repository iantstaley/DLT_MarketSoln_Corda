package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.utilities.ProgressTracker;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class PrintHello extends FlowLogic<String> {

    @Suspendable
    @Override
    public String call() throws FlowException {
        // Initiator flow logic goes here.

        return "Hello";
    }
}
