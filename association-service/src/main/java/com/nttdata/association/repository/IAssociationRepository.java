package com.nttdata.association.repository;

import com.nttdata.association.model.entity.Association;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IAssociationRepository extends ReactiveMongoRepository<Association, String> {
}
