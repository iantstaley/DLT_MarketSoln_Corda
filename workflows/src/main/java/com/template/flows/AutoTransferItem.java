package com.template.flows;

import com.template.states.DeliveryRespondState;
import net.corda.core.contracts.TransactionState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@CordaService
public class AutoTransferItem extends SingletonSerializeAsToken {

    private final static Logger log = LoggerFactory.getLogger(AutoTransferItem.class);
    private final static Executor executor = Executors.newFixedThreadPool(8);
    private final AppServiceHub serviceHub;

    public AutoTransferItem(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
        directTransfer();
        log.info("Tracking new Payment Request");
    }

    private void directTransfer() {
        Party ourIdentity = ourIdentity();
        serviceHub.getVaultService().trackBy(DeliveryRespondState.class).getUpdates().subscribe(
                update -> {
                    update.getProduced().forEach(
                            message -> {
                                TransactionState<DeliveryRespondState> state = message.getState();
                                if(ourIdentity.equals(
                                        serviceHub.getNetworkMapCache().getPeerByLegalName(new CordaX500Name("Shop","Kolkata","IN"))
                                )) {
                                    executor.execute(() -> {
                                        log.info("Directing to message " + state);
                                        serviceHub.startFlow(new TransferItem(
                                                state.getData().getLinearId().getId(),
                                                state.getData().getProductKey(),
                                                state.getData().getShopName(),
                                                state.getData().getCarrierName()
                                        ));
                                    });
                                }
                            }
                    );
                }
        );
    }

    private Party ourIdentity() {
        return serviceHub.getMyInfo().getLegalIdentities().get(0);
    }
}
