package com.nttdata.association.service.impl;

import com.nttdata.association.model.entity.Association;
import com.nttdata.association.repository.IAssociationRepository;
import com.nttdata.association.service.IAssociationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AssociationServiceImpl implements IAssociationService {

    private final IAssociationRepository repository;

    @Override
    public Mono<Association> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Association> save(Association association) {
        return repository.save(association);
    }
}
