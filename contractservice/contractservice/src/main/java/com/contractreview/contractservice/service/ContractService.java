package com.contractreview.contractservice.service;

import com.contractreview.contractservice.entity.Contract;
import com.contractreview.contractservice.event.ContractUploadedEvent;
import com.contractreview.contractservice.repository.ContractRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ContractService {

    @Autowired
    private GridFsOperations gridFsOperations;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public String uploadContract(MultipartFile file, String userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }

        log.debug("Starting upload | file: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        // 1. Store the actual file content in GridFS
        ObjectId gridFsId = gridFsOperations.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        String gridFsFileId = gridFsId.toHexString();
        log.info("File stored in GridFS | id: {}", gridFsFileId);

        // 2. Create metadata entity
        Contract contract = new Contract();
        contract.setContractId(UUID.randomUUID().toString());
        contract.setUserId(userId);
        contract.setOriginalFileName(file.getOriginalFilename());
        contract.setContentType(file.getContentType());
        contract.setFileSizeBytes(file.getSize());
        contract.setUploadDate(LocalDateTime.now());
        contract.setStatus("UPLOADED");
        contract.setGridFsFileId(gridFsFileId);

        log.info("Attempting to save metadata | contractId: {}, userId: {}, file: {}",
                contract.getContractId(), userId, file.getOriginalFilename());

        // 3. Save metadata to MongoDB
        Contract savedContract;
        try {
            savedContract = contractRepository.save(contract);
            log.info("SAVE SUCCESS ✅ | Mongo _id: {}, contractId: {}, userId: {}",
                    savedContract.getId(), savedContract.getContractId(), savedContract.getUserId());

            if (savedContract.getId() == null) {
                log.warn("Warning: Saved contract has null _id - possible MongoDB issue");
            }

        } catch (Exception e) {
            log.error("MONGO SAVE FAILED  | File: {}, User: {}, Error: {}",
                    file.getOriginalFilename(), userId, e.getMessage(), e);
            throw new RuntimeException("Failed to save contract metadata to MongoDB", e);
        }

        // 4. Publish Kafka event (uncomment when Jackson/Kafka is fixed)
        ContractUploadedEvent event = new ContractUploadedEvent(
                savedContract.getContractId(),
                savedContract.getUserId(),
                savedContract.getGridFsFileId(),
                savedContract.getOriginalFileName(),
                savedContract.getContentType(),
                savedContract.getFileSizeBytes()
        );

        // kafkaProducerService.sendContractUploadedEvent(event);
        // log.info("Kafka event published | contractId: {}", savedContract.getContractId());

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
            log.warn("GridFS file not found for id: {}", gridFsFileId);
            throw new IOException("File not found in GridFS for id: " + gridFsFileId);
        }

        GridFsResource gridFsResource = gridFsOperations.getResource(file);
        return new InputStreamResource(gridFsResource.getInputStream());
    }
}
