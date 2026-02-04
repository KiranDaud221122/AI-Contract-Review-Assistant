package com.contractreview.contractservice.service;

import com.contractreview.contractservice.entity.Contract;
import com.contractreview.contractservice.event.ContractUploadedEvent;
import com.contractreview.contractservice.repository.ContractRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final GridFsOperations gridFsOperations;
    private final ContractRepository contractRepository;
    private final KafkaProducerService kafkaProducerService;  // renamed for clarity

    public String uploadContract(MultipartFile file, String userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }

        log.debug("Starting upload | file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

        // 1. Store file in GridFS
        ObjectId gridFsId = gridFsOperations.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        String gridFsFileId = gridFsId.toHexString();
        log.info("File stored in GridFS | id: {}", gridFsFileId);

        // 2. Create and save metadata
        Contract contract = new Contract();
        contract.setContractId(UUID.randomUUID().toString());  // unique contract ID
        contract.setUserId(userId);
        contract.setOriginalFileName(file.getOriginalFilename());
        contract.setContentType(file.getContentType());
        contract.setFileSizeBytes(file.getSize());
        contract.setUploadDate(LocalDateTime.now());
        contract.setStatus("UPLOADED");
        contract.setGridFsFileId(gridFsFileId);

        log.info("Saving contract metadata | contractId: {}, userId: {}", contract.getContractId(), contract.getUserId());

        Contract savedContract = contractRepository.save(contract);
        log.info("Contract saved successfully | id: {}, contractId: {}", savedContract.getId(), savedContract.getContractId());

        // 3. Publish Kafka event (now uncommented + safe)
        try {
            ContractUploadedEvent event = new ContractUploadedEvent(
                    savedContract.getContractId(),
                    savedContract.getUserId(),
                    savedContract.getGridFsFileId(),
                    savedContract.getOriginalFileName(),
                    savedContract.getContentType(),
                    savedContract.getFileSizeBytes()
            );
            kafkaProducerService.sendContractUploadedEvent(event);
            log.info("Kafka event published successfully | contractId: {}", savedContract.getContractId());
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for contractId: {}", savedContract.getContractId(), e);
            // Optional: mark contract status as "EVENT_FAILED" if you want to retry later
        }

        return savedContract.getContractId();
    }

    public Optional<Contract> getContractById(String contractId) {
        return contractRepository.findByContractId(contractId);
    }

    public List<Contract> getContractsByUser(String userId) {
        return contractRepository.findByUserId(userId);
    }

    public Resource getFileResource(String gridFsFileId) throws IOException {
        GridFSFile file = gridFsOperations.findOne(
                Query.query(Criteria.where("_id").is(new ObjectId(gridFsFileId)))
        );
        if (file == null) {
            throw new IOException("File not found in GridFS for id: " + gridFsFileId);
        }
        GridFsResource gridFsResource = gridFsOperations.getResource(file);
        return new InputStreamResource(gridFsResource.getInputStream());
    }
}