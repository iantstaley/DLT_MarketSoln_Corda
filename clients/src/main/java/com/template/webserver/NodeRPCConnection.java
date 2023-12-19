package com.template.webserver;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 */
@Component
public class NodeRPCConnection implements AutoCloseable {
    // The host of the node we are connecting to.
    @Value("${config.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${config.rpc.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${config.rpc.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${config.rpc.port}")
    private int rpcPort;

    private CordaRPCConnection rpcConnection;
    CordaRPCOps proxy;

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        rpcConnection = rpcClient.start(username, password);
        proxy = rpcConnection.getProxy();
    }

    @PreDestroy
    public void close() {
        rpcConnection.notifyServerAndClose();
    }
}

//###################################################################################

//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import net.corda.client.jackson.JacksonSupport;
//import net.corda.client.rpc.CordaRPCClient;
//import net.corda.core.messaging.CordaRPCOps;
//import net.corda.core.utilities.NetworkHostAndPort;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class NodeRPCConnection implements WebMvcConfigurer {
//
//    @Value("${buyer.host}")
//    private String buyerHostAndPort;
//
//    @Value("${delivery.host}")
//    private String deliveryHostAndPort;
//
//   @Value("${bank.host}")
//   private String bankHostAndPort;
//
//    @Value("${shop.host}")
//    private String shopHostAndPort;
//
//    @Bean(destroyMethod = "")  //Avoids node shutdown on rpc disconnect
//    public CordaRPCOps buyerProxy() {
//        CordaRPCClient buyerClient = new CordaRPCClient(NetworkHostAndPort.parse(buyerHostAndPort));
//        return buyerClient.start("user1","test").getProxy();
//    }
//
//    @Bean(destroyMethod = "")
//    public CordaRPCOps deliveryProxy() {
//        CordaRPCClient deliveryClient = new CordaRPCClient(NetworkHostAndPort.parse(deliveryHostAndPort));
//        return deliveryClient.start("user1","test").getProxy();
//    }
//
//   @Bean(destroyMethod = "")
//   public CordaRPCOps bankProxy() {
//       CordaRPCClient bankClient = new CordaRPCClient(NetworkHostAndPort.parse(bankHostAndPort));
//       return bankClient.start("user1","test").getProxy();
//   }
//
//    @Bean(destroyMethod = "")
//    public CordaRPCOps shopProxy() {
//        CordaRPCClient shopClient = new CordaRPCClient(NetworkHostAndPort.parse(shopHostAndPort));
//        return shopClient.start("user1","test").getProxy();
//    }
//
//    /**
//     * Corda Jackson Support, to convert corda objects to json
//     */
//    @Bean
//    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(){
//        ObjectMapper mapper =  JacksonSupport.createDefaultMapper(buyerProxy());
//        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
//        converter.setObjectMapper(mapper);
//        return converter;
//    }
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**");
//    }
//}