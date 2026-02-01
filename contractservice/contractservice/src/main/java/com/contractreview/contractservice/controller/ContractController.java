package com.contractreview.contractservice.controller;

import com.contractreview.contractservice.entity.Contract;
import com.contractreview.contractservice.service.ContractService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/api/v1/contracts")
@AllArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * Upload a contract file (PDF, DOCX, etc.)
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", defaultValue = "Welcome to Kiran's Era") String userId) {

        try {
            String contractId = contractService.uploadContract(file, userId);
            log.info("Upload Success | contractId: {}", contractId);
            return ResponseEntity.ok(contractId);
        } catch (IOException e) {
            log.error("Upload failed due to IO error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * View or download the uploaded file by contractId
     * - Opens in browser (PDFs usually inline)
     * - Use ?download=true to force download
     */
    @GetMapping("/view/{contractId}")
    public ResponseEntity<Resource> viewFile(
            @PathVariable String contractId,
            @RequestParam(value = "download", defaultValue = "false") boolean download) {

        log.info("View request for contractId: {}, download={}", contractId, download);

        Optional<Contract> optionalContract = contractService.getContractById(contractId);
        if (optionalContract.isEmpty()) {
            log.warn("Contract not found: {}", contractId);
            return ResponseEntity.notFound().build();
        }

        Contract contract = optionalContract.get();

        try {
            Resource resource = contractService.getFileResource(contract.getGridFsFileId());

            HttpHeaders headers = new HttpHeaders();
            String disposition = download ? "attachment" : "inline";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + contract.getOriginalFileName() + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contract.getContentType()))
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to retrieve file from GridFS for contractId: {}", contractId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Get all contracts uploaded by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Contract>> getUserContracts(@PathVariable String userId) {
        List<Contract> contracts = contractService.getContractsByUser(userId);
        if (contracts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(contracts);
    }
}