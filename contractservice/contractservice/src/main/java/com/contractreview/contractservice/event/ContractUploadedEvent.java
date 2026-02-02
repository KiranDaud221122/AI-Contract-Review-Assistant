package com.contractreview.contractservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractUploadedEvent implements Serializable {

    private String contractId;
    private String userId;
    private String gridFsFileId;
    private String originalFileName;
    private String contentType;
    private long fileSIzeBytes;



}