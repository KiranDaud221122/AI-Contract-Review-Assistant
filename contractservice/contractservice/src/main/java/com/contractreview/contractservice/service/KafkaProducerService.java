package com.contractreview.contractservice.service;

import com.contractreview.contractservice.event.ContractUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, ContractUploadedEvent> kafkaTemplate;
    @Value("${kafka.topic.name}")
    private String topic;

    public void sendContractUploadedEvent(ContractUploadedEvent event) {
        kafkaTemplate.send(topic, event.getContractId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka event published | contractId: {}", event.getContractId());
                    } else {
                        log.error("Kafka publish failed | contractId: {}", event.getContractId(), ex);
                    }
                });
    }
}