package com.nttdata.association.handler;

import com.nttdata.association.exception.AssociationException;
import com.nttdata.association.model.dto.AccountDto;
import com.nttdata.association.model.dto.AssociationDto;
import com.nttdata.association.model.entity.Association;
import com.nttdata.association.service.IAccountService;
import com.nttdata.association.service.IAssociationService;
import com.nttdata.association.service.ICreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AssociationHandler {

    private final IAssociationService service;
    private final IAccountService iAccountService;
    private final ICreditCardService iCreditCardService;

    public Mono<ServerResponse> creditCardCreate(ServerRequest request){
        Mono<Association> associationMono = request.bodyToMono(Association.class);
        return associationMono
                .flatMap(service::save)
                .flatMap(a -> ServerResponse.created(URI.create("/association/".concat(a.getId())))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(a)
                );
    }

    public Mono<ServerResponse> debitCardAccountAssociationCreate(ServerRequest request){
        Mono<AssociationDto> associationDtoMono = request.bodyToMono(AssociationDto.class);
        Association association = new Association();
        return associationDtoMono
                .flatMap(dto -> iCreditCardService.findByCardNumber(dto.getCardNumber())
                                                    .flatMap(card -> {
                                                        if (card.getCardType().equals("CREDIT")){
                                                            return Mono.error(
                                                                    new WebClientResponseException(400,
                                                                            "Solo se pueden asociar tarjetas debito",
                                                                            null,null,null)
                                                            );
                                                        }
                                                        association.setCardNumber(card.getCardNumber());
                                                        association.setCardType(card.getCardType());
                                                        return Mono.just(dto);
                                                    })
                                                    .onErrorResume(error -> {
                                                        WebClientResponseException errorResponse = (WebClientResponseException) error;
                                                        if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                                                            return Mono.error(
                                                                    new WebClientResponseException(404,
                                                                            "El numero de la tarjeta es incorrecto",
                                                                            null,null,null)
                                                            );
                                                        }
                                                        return Mono.error(errorResponse);
                                                    })
                )
                .flatMap(dto ->
                        Flux.fromIterable(dto.getAccountList())
                            .filter(AccountDto::isPrincipal)
                            .count()
                            .flatMap(c -> {
                                //Check if there are two or more main accounts in the accounts to associate
                                if (Math.toIntExact(c) > 1)
                                    return Mono.error(new WebClientResponseException(400, "Solo debe haber una cuenta principal", null,null,null));
                                else
                                    return service.findByCardNumberAndStatus(association.getCardNumber(), "ASSOCIATED")
                                            .filter(Association::isPrincipal)
                                            .count()
                                            .flatMap(ca -> {
                                                //Check if the debit card has an associated main account
                                               if (Math.toIntExact(ca) > 0){
                                                   //Check if having a main account on the card will associate another main account
                                                   if (Math.toIntExact(c) == 1)
                                                       return Mono.error(new WebClientResponseException(400, "Actualmente hay una cuenta principal", null,null,null));
                                               } else if (Math.toIntExact(ca) == 0){
                                                   //Check if the card does not have a main account and a main account will be associated with it
                                                   if (Math.toIntExact(c) == 0)
                                                       return Mono.error(new WebClientResponseException(400, "Debe asignar la cuenta principal", null,null,null));
                                               }
                                               return Mono.just(dto);
                                            });
                            })
                )
                .flatMap(dto -> Flux.fromIterable(dto.getAccountList())
                        .flatMap(a ->
                                service.findByAccountNumberAndStatusAndCardNumberNot(a.getAccountNumber(), "ASSOCIATED", dto.getCardNumber())
                                .count()
                                .flatMap(c -> {
                                    if (Math.toIntExact(c) > 0){
                                        return Mono.error(new WebClientResponseException(400, "La cuenta Nº " + a.getAccountNumber() + " esta asociada a otra tarjeta", null,null,null));
                                    }
                                    return Mono.just(a);
                                }))
                        .collectList())
                .flatMap(list ->
                        Flux.fromIterable(list)
                        //.delayElements(Duration.ofMillis(500))
                        .delayElements(Duration.ofMillis(50))
                        .flatMap(a ->
                                service.findByCardNumberAndAccountNumber(association.getCardNumber(), a.getAccountNumber())
                                .flatMap(ass -> {
                                    if (ass.getStatus().equals("DISASSOCIATED")){
                                        ass.setStatus("ASSOCIATED");
                                        ass.setCreationDate(LocalDateTime.now());
                                        return service.save(ass);
                                    }
                                    return Mono.just(ass);
                                })
                                .switchIfEmpty(iAccountService.findByAccountNumber(a.getAccountNumber())
                                                                .flatMap(acc -> {
                                                                    Association assoc = new Association();
                                                                    assoc.setCardNumber(association.getCardNumber());
                                                                    assoc.setAccountNumber(acc.getAccountNumber());
                                                                    assoc.setCardType(association.getCardType());
                                                                    assoc.setAccountType(acc.getAccountType());
                                                                    assoc.setDocumentNumber(acc.getCustomer().getDocumentNumber());
                                                                    assoc.setPrincipal(a.isPrincipal());
                                                                    assoc.setStatus("ASSOCIATED");
                                                                    return service.save(assoc);
                                                                })
                                        ))
                        .collectList())
                .flatMap(a -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(a)
                )
                .onErrorResume(AssociationException::errorHandler);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");
        return service.findById(id)
                .flatMap(a -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(a)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByAccountNumberAndStatus(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        String status = request.pathVariable("status");
        return service.findByAccountNumberAndStatus(accountNumber, status)
                .flatMap(a -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(a)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByAccountNumberAndStatusAndCardNumberNot(ServerRequest request){
        String accountNumber = request.pathVariable("accountNumber");
        String status = request.pathVariable("status");
        String cardNumber = request.pathVariable("cardNumber");
        return service.findByAccountNumberAndStatusAndCardNumberNot(accountNumber, status, cardNumber)
                .collectList()
                .flatMap(a -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(a)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByCardNumberAndStatus(ServerRequest request){
        String cardNumber = request.pathVariable("cardNumber");
        String status = request.pathVariable("status");
        return service.findByCardNumberAndStatus(cardNumber, status)
                .collectList()
                .flatMap(ass -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ass)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}
