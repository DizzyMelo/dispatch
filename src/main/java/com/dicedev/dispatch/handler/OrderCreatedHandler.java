package com.dicedev.dispatch.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.dicedev.dispatch.message.OrderCreated;
import com.dicedev.dispatch.service.DispatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCreatedHandler {
    
    private final DispatchService dispatchService;

    @KafkaListener(
        id = "orderConsumerClient",
        topics = "order.created",
        groupId = "dispatch.order.created.consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(OrderCreated payload) {
        log.info("Received message payload: " + payload);
        try {
            dispatchService.process(payload);
        } catch (Exception e) {
            log.error("Error processing order created message", e);
        }
    }

}
