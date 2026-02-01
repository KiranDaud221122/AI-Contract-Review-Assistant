package com.contractreview.aiservice.repository;

import com.contractreview.aiservice.entity.ReviewResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewResultRepository extends MongoRepository<ReviewResult,String> {
    List<ReviewResult> findByUserId(String userId);
    Optional<ReviewResult> findByContractId(String contractId);
}
