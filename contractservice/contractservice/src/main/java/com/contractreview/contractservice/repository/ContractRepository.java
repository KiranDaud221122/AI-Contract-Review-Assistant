package com.contractreview.contractservice.repository;

import com.contractreview.contractservice.entity.Contract;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends MongoRepository<Contract , String> {
    Optional<Contract> findByContractId(String contractId);

    List<Contract> findByUserId(String userId);

    List<Contract> findByUserIdAndStatus(String userId, String status);
}


