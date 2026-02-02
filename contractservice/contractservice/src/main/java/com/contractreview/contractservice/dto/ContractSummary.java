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
public class ContractSummary {

    private String contractId;
    private String originalFileName;
    private long fileSizeBytes;
    private LocalDateTime uploadDate;
    private String status;
}
