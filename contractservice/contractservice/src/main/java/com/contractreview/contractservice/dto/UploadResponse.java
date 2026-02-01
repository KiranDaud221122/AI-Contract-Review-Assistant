// src/main/java/com/contractreview/contractservice/dto/UploadResponse.java
package com.contractreview.contractservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private String contractId;
    private String originalFileName;
    private String contentType;
    private long fileSizeBytes;
    private String status;
    private LocalDateTime uploadDate;
    private String message;
}