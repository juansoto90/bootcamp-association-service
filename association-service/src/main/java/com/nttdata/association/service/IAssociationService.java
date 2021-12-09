package com.nttdata.association.service;

import com.nttdata.association.model.entity.Association;
import reactor.core.publisher.Mono;

public interface IAssociationService {
    public Mono<Association> findById(String id);
    public Mono<Association> save(Association association);
}
