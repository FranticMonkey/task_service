package com.example.task_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.task-events}")
    private String taskEventsTopic;

    public void sendTaskEvent(Object event) {
        kafkaTemplate.send(taskEventsTopic, event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent event to Kafka: {}", event);
            } else {
                log.error("Failed to send event to Kafka: {}", event, ex);
            }
        });
    }
}
