package com.nttdata.association.repository;

import com.nttdata.association.model.entity.Association;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAssociationRepository extends ReactiveMongoRepository<Association, String> {
    public Mono<Association> findByAccountNumberAndStatus(String accountNumber, String status);
    public Flux<Association> findByCardNumberAndStatus(String cardNumber, String status);
    public Mono<Association> findByCardNumberAndAccountNumber(String cardNumber, String accountNumber);
    public Flux<Association> findByAccountNumberAndStatusAndCardNumberNot(String accountNumber, String status, String cardNumber);
}
