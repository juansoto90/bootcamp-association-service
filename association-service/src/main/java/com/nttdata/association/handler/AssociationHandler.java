package com.nttdata.association.handler;

import com.nttdata.association.model.entity.Association;
import com.nttdata.association.service.IAssociationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class AssociationHandler {

    private final IAssociationService service;

    public Mono<ServerResponse> create(ServerRequest request){
        Mono<Association> associationMono = request.bodyToMono(Association.class);
        return associationMono
                .flatMap(service::save)
                .flatMap(a -> ServerResponse.created(URI.create("/association/".concat(a.getId())))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(a)
                );
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

}
