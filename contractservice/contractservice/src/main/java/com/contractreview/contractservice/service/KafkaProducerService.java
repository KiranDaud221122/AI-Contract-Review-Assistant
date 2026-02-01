package com.contractreview.contractservice.service;

import com.contractreview.contractservice.event.ContractUploadedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaProducerService {

    private static final String TOPIC_NAME= "contract-uploaded";

    private final KafkaTemplate<String, ContractUploadedEvent> kafkaTemplate;

    public void sendContractUploadedEvent (ContractUploadedEvent event){
        try{
            kafkaTemplate.send(TOPIC_NAME, event.getContractId(),event);
            log.info("Kafka event publish | contractId: {}, file: {}",event.getContractId(),event.getOriginalFileName());
        }
        catch (Exception ex){
            log.error(" Failed to publish Kafka event for contractId: {}",
                    event.getContractId(), ex);

        }
    }

}
