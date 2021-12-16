package com.nttdata.association.service;

import com.nttdata.association.model.entity.CreditCard;
import reactor.core.publisher.Mono;

public interface ICreditCardService {
    public Mono<CreditCard> findByCardNumber(String cardNumber);
}
