package com.contractreview.contractservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "contracts")
public class Contract {

    @Id
    private String id;
    private String contractId;
    private String userId;
    private String originalFileName;
    private String contentType;
    private long fileSizeBytes;
    private LocalDateTime uploadDate;
    private String status;
    private String gridFsFileId;

}
