package com.nttdata.association.service.impl;

import com.nttdata.association.model.entity.Account;
import com.nttdata.association.service.IAccountService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AccountServiceImpl implements IAccountService {

    private final WebClient.Builder webClientBuilder;
    private final String WEB_CLIENT_URL = "microservice.web.account";
    private final String BASE;

    public AccountServiceImpl(WebClient.Builder webClientBuilder, Environment env) {
        this.webClientBuilder = webClientBuilder;
        BASE = env.getProperty(WEB_CLIENT_URL);
    }

    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        return webClientBuilder
                .baseUrl(BASE)
                .build()
                .get()
                .uri("/account-number/{accountNumber}", accountNumber)
                .retrieve()
                .bodyToMono(Account.class);
    }
}
