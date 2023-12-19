package com.template.webserver;

import com.template.flows.*;
import com.template.states.HandOverRequestState;
import com.template.states.ItemState;
import com.template.states.OrderState;
import com.template.states.RequestDeliveryState;
import com.template.webserver.models.*;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Define your API endpoints here.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {

    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }

    @GetMapping(value = "/templateendpoint", produces = "text/plain")
    private String templateendpoint() {
        return "Define an endpoint here.";
    }

    //////////////////////////////////////////////////

//    @Autowired
//    private CordaRPCOps buyerProxy;
//
//    @Autowired
//    private CordaRPCOps deliveryProxy;
//
//   @Autowired
//   private CordaRPCOps bankProxy;
//
//    @Autowired
//    private CordaRPCOps shopProxy;
//
//    @Autowired
//    @Qualifier("buyerProxy")
//    private CordaRPCOps proxy;

//    @PostMapping(value = "/switch-party/{party}")
//    private String switchParty(@PathVariable String party) {
//        if(party.equalsIgnoreCase("shop"))
//            proxy = shopProxy;
//        else if(party.equalsIgnoreCase("delivery"))
//            proxy = deliveryProxy;
//        else if(party.equalsIgnoreCase("buyer"))
//            proxy = buyerProxy;
//        else if(party.equalsIgnoreCase("bank"))
//            proxy = bankProxy;
//        return proxy.nodeInfo().getLegalIdentities().get(0).getName().toString();
//    }

    /////////////////////////////////////////////////////

    @GetMapping(value = "/whoAmI")
    private String whoAmI() {
        return proxy.nodeInfo().getLegalIdentities().get(0).getName().toString();
    }


    @PostMapping(value = "/createShopAccount")
    private ResponseEntity createShopAccount(@RequestBody ShopModel body) throws ExecutionException, InterruptedException {

        if(!proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop"))
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        Party delivery = proxy.partiesFromName("Delivery",false).iterator().next();
        Party buyer = proxy.partiesFromName("Buyer",false).iterator().next();
        Party bank = proxy.partiesFromName("Bank",false).iterator().next();

        CordaFuture<String> st = proxy.startFlowDynamic(CreateNewAccount.class,
                body.getShopName(),
                Arrays.asList(delivery,buyer,bank)).getReturnValue();

        if(st.get().equalsIgnoreCase("Creation Failed"))
            return new ResponseEntity(null,HttpStatus.BAD_REQUEST);
        else
            return new ResponseEntity("Success",HttpStatus.OK);
    }


    @PostMapping(value = "/createUserAccount")
    private ResponseEntity createUserAccount(@RequestBody UserModel body) throws ExecutionException, InterruptedException {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Buyer")) {
            Party shop = proxy.partiesFromName("Shop",false).iterator().next();
            Party delivery = proxy.partiesFromName("Delivery",false).iterator().next();
            Party bank = proxy.partiesFromName("Bank",false).iterator().next();

            String res = proxy.startFlowDynamic(CreateNewAccount.class,
                    body.getUserName(),
                    Arrays.asList(shop,delivery,bank)).getReturnValue().get();

            if(res.equalsIgnoreCase("Creation Failed"))
                return new ResponseEntity(null,HttpStatus.BAD_REQUEST);
            else
                return new ResponseEntity("Success",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/createDeliveryAccount")
    private ResponseEntity createDeliveryAccount(@RequestBody DeliveryGuyModel body) throws ExecutionException, InterruptedException {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Delivery")) {
            Party shop = proxy.partiesFromName("Shop",false).iterator().next();
            Party buyer = proxy.partiesFromName("Buyer",false).iterator().next();
            Party bank = proxy.partiesFromName("Bank",false).iterator().next();

            String res = proxy.startFlowDynamic(CreateNewAccount.class,
                    body.getDeliveryPersonName(),
                    Arrays.asList(shop,buyer,bank)).getReturnValue().get();

            if(res.equalsIgnoreCase("Creation Failed"))
                return new ResponseEntity(null,HttpStatus.BAD_REQUEST);
            else
                return new ResponseEntity("Success",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/addItem")
    private ResponseEntity addItem(@RequestBody ItemModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop")) {
            UUID key = UUID.randomUUID();
            proxy.startFlowDynamic(AddItem.class,key,
                    body.getProductName(),
                    body.getProductDetails(),
                    body.getPrice(),
                    body.getExpiryDate(),
                    body.getQuantity(),
                    body.getBarCode(),
                    body.getShopAccountName());
            return new ResponseEntity(key.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping(value = "/viewInventory/{shopAccountName}")
    private ResponseEntity viewInventory(@PathVariable String shopAccountName) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop")) {
            QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

            List<StateAndRef<ItemState>> allItemStateAndRefs = proxy.vaultQueryByCriteria(criteria,ItemState.class).getStates();

            List<InventoryModel> inventory = new ArrayList<>();

            allItemStateAndRefs.stream().filter(it -> it.getState().getData().getShopAccountName().equals(shopAccountName)).forEach(
                    it -> {
                        QueryCriteria.LinearStateQueryCriteria linearQueryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                                null,
                                Collections.singletonList(it.getState().getData().getLinearId()),
                                Vault.StateStatus.UNCONSUMED,
                                null
                        );

                        String timeOfAddition = proxy.vaultQueryByCriteria(linearQueryCriteria,ItemState.class)
                                .getStatesMetadata().get(0).getRecordedTime().toString();

                        inventory.add(new InventoryModel(
                                it.getState().getData().getLinearId().getId().toString(),
                                it.getState().getData().getProductName(),
                                it.getState().getData().getProductDetails(),
                                it.getState().getData().getExpiryDate(),
                                it.getState().getData().getQuantity(),
                                it.getState().getData().getPrice(),
                                timeOfAddition
                                ));
                    }
            );
            return new ResponseEntity(inventory,HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.OK);
        }
    }

    @PostMapping(value = "/placeOrder")
    private ResponseEntity placeOrder(@RequestBody OrderModel body) throws ExecutionException, InterruptedException {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Buyer")) {

            String orderId = proxy.startFlowDynamic(PlaceOrder.class,
                    UUID.fromString(body.getProductKey()),
                    body.getBuyerAccountName(),
                    body.getShopAccountName(),
                    body.getDeliveryAddress(),
                    body.getAmtToShop(),
                    body.getAmtToDelivery()).getReturnValue().get();

            return new ResponseEntity(orderId,HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping(value = "/getOrders/{shopAccountName}")
    private ResponseEntity getOrders(@PathVariable String shopAccountName) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop")) {
            QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

            List<StateAndRef<OrderState>> allOrderStateAndRef = proxy.vaultQueryByCriteria(criteria,OrderState.class).getStates();

            List<ReceivedOrderModel> receivedOrders = new ArrayList<ReceivedOrderModel>();

            allOrderStateAndRef.stream().forEach(it -> {
                if(it.getState().getData().getShopAccountName().equals(shopAccountName)) {
                    receivedOrders.add(new ReceivedOrderModel(
                            it.getState().getData().getLinearId().getId().toString(),
                            it.getState().getData().getProductKey().toString(),
                            it.getState().getData().getUserAccountName()
                    ));
                } });

            return new ResponseEntity(receivedOrders,HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/requestDelivery")
    private ResponseEntity acceptOrder(@RequestBody AcceptOrderModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop")) {
            proxy.startFlowDynamic(RequestDelivery.class,
                    UUID.fromString(body.getTrackingId()),
                    body.getShopName(),
                    "Delivery");
            return new ResponseEntity("Success",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/acceptDelivery")
    private ResponseEntity acceptDelivery(@RequestBody AcceptDeliveryModel body) throws ExecutionException, InterruptedException {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Delivery")) {
            String st = proxy.startFlowDynamic(AcceptDelivery.class,
                    UUID.fromString(body.getOrderId()),
                    body.getBarCode(),
                    body.getAcceptor(),
                    body.getShopAccountName()).getReturnValue().get();

            if (st.equalsIgnoreCase("Success"))
                return new ResponseEntity("Success",HttpStatus.OK);
            else
                return new ResponseEntity("Wrong Bar Code",HttpStatus.NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping(value = "/receivedDeliveryRequests")
    private ResponseEntity receivedDeliveryRequests() {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Delivery")) {
            QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

            List<StateAndRef<RequestDeliveryState>> allRequestDeliveryStateAndRefs = proxy.vaultQueryByCriteria(criteria,RequestDeliveryState.class).getStates();

            List<ReceivedDeliveryRequestModel> receivedDeliveryRequests = new ArrayList<ReceivedDeliveryRequestModel>();

            allRequestDeliveryStateAndRefs.stream().forEach(it ->
                    receivedDeliveryRequests.add(new ReceivedDeliveryRequestModel(
                            it.getState().getData().getLinearId().getId().toString(),
                            it.getState().getData().getShopAccountName(),
                            it.getState().getData().getBuyerAccountName(),
                            it.getState().getData().getBuyerAddress()
                    )));

            return new ResponseEntity(receivedDeliveryRequests,HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping(value = "/getLeftHandovers/{deliveryAccountName}")
    private ResponseEntity getLeftHandovers(@PathVariable String deliveryAccountName) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Delivery")) {
            QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

            List<StateAndRef<HandOverRequestState>> allHandOverRequestStates = proxy.vaultQueryByCriteria(criteria,HandOverRequestState.class).getStates();
            List<HandoverModel> handoverModels = new ArrayList<HandoverModel>();

            allHandOverRequestStates.stream().forEach(it -> {
                if(it.getState().getData().getDeliveryGuyAccountName().equals(deliveryAccountName)) {
                    handoverModels.add(new HandoverModel(
                            it.getState().getData().getLinearId().getId().toString(),
                            it.getState().getData().getBuyerAccountName(),
                            it.getState().getData().getDeliveryAddress()
                    ));
                }
            });

            return new ResponseEntity(handoverModels,HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/handoverItem")
    private ResponseEntity handoverItem(@RequestBody FinalHandoverModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Delivery")) {
            QueryCriteria.LinearStateQueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(
                    null,
                    Collections.singletonList(new UniqueIdentifier(null,UUID.fromString(body.getTrackingId()))),
                    Vault.StateStatus.UNCONSUMED,
                    null
            );

            StateAndRef<HandOverRequestState> handOverRequestStateStateAndRef = proxy.vaultQueryByCriteria(criteria,HandOverRequestState.class).getStates().get(0);

            if(handOverRequestStateStateAndRef.getState().getData().getDeliveryGuyAccountName().equalsIgnoreCase(body.getDeliveryAccountName())) {
                proxy.startFlowDynamic(HandoverItem.class,UUID.fromString(body.getTrackingId()));
                return new ResponseEntity("Success",HttpStatus.OK);
            } else {
                return new ResponseEntity("Wrong Delivery Person",HttpStatus.NOT_ACCEPTABLE);
            }

        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/verifyProduct")
    private ResponseEntity verifyProduct(@RequestBody VerificationModel body) throws ExecutionException, InterruptedException {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Buyer")) {

            String st = proxy.startFlowDynamic(VerifyProduct.class,UUID.fromString(body.getProductKey()),body.getBarCode()).getReturnValue().get();

            if(st.equalsIgnoreCase("Success"))
                return new ResponseEntity("Success",HttpStatus.OK);
            else
                return new ResponseEntity("Failed",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping(value = "/getBalance/{accountName}")
    private String getBalance(@PathVariable String accountName) throws ExecutionException, InterruptedException {
        String st = proxy.startFlowDynamic(GetBalance.class,accountName).getReturnValue().get();
        return st;
    }

    @PostMapping(value = "/issueCoin/")
    private ResponseEntity issueCoin(@RequestBody IssueCoinModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Bank")) {
            proxy.startFlowDynamic(IssueCoin.class,"Bank",body.getAccountName(),body.getValue());
            return new ResponseEntity("Success",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/rejectOrder")
    private ResponseEntity rejectOrder(@RequestBody RejectOrderModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop")) {
            proxy.startFlowDynamic(IssueRefundState.class,UUID.fromString(body.getProductKey()),body.getAccountName(),"Bank");
            return new ResponseEntity("Success",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping(value = "/requestRefund")
    private ResponseEntity requestRefund(@RequestBody RejectOrderModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Buyer")) {
            proxy.startFlowDynamic(IssueRefundState.class,UUID.fromString(body.getProductKey()),body.getAccountName(),"Bank");
            return new ResponseEntity("Success",HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

}