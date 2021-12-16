package com.nttdata.association.service;

import com.nttdata.association.model.entity.Account;
import reactor.core.publisher.Mono;

public interface IAccountService {
    public Mono<Account> findByAccountNumber(String accountNumber);
}
