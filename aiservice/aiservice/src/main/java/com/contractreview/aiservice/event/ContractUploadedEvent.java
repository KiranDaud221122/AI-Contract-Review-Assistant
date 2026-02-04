
package com.contractreview.aiservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractUploadedEvent {
    private String contractId;
    private String userId;
    private String gridFsFileId;
    private String originalFileName;
    private String contentType;
    private long fileSizeBytes;
}