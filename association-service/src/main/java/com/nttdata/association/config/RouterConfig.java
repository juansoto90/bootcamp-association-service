package com.nttdata.association.config;

import com.nttdata.association.handler.AssociationHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> routes(AssociationHandler handler){
        return route(GET("/association/{id}"), handler::findById)
                .andRoute(GET("/association/{accountNumber}/{status}"), handler::findByAccountNumberAndStatus)
                .andRoute(GET("/association/card/{cardNumber}/{status}"), handler::findByCardNumberAndStatus)
                .andRoute(GET("/association/{accountNumber}/{status}/{cardNumber}"), handler::findByAccountNumberAndStatusAndCardNumberNot)
                .andRoute(POST("/association/credit-card"), handler::creditCardCreate)
                .andRoute(POST("/association/debit-card"), handler::debitCardAccountAssociationCreate);
    }
}
