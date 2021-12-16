package com.nttdata.association.service;

import com.nttdata.association.model.entity.Association;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAssociationService {
    public Mono<Association> findById(String id);
    public Mono<Association> findByAccountNumberAndStatus(String accountNumber, String status);
    public Flux<Association> findByCardNumberAndStatus(String cardNumber, String status);
    public Mono<Association> findByCardNumberAndAccountNumber(String cardNumber, String accountNumber);
    public Flux<Association> findByAccountNumberAndStatusAndCardNumberNot(String accountNumber, String status, String cardNumber);
    public Mono<Association> save(Association association);
}
