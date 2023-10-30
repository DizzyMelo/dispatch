package com.dicedev.dispatch.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.dicedev.dispatch.message.DispatchPreparing;
import com.dicedev.dispatch.message.OrderCreated;
import com.dicedev.dispatch.message.OrderDispatched;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private static final String ORDER_DISPATCH_TRACKING = "dispatch.tracking";
    private static final UUID APPLICATION_ID = UUID.randomUUID();

    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(String key, OrderCreated orderCreated) throws Exception{
        DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
            .id(orderCreated.getId())
            .build();

        kafkaProducer.send(ORDER_DISPATCH_TRACKING, key, dispatchPreparing).get();
        
        OrderDispatched orderDispatched = OrderDispatched.builder()
            .id(orderCreated.getId())
            .processedById(APPLICATION_ID)
            .notes("Dispatched: " + orderCreated.getItem())
            .build();

        kafkaProducer.send(ORDER_DISPATCHED_TOPIC, key, orderDispatched).get();

        log.info("Sent message: key: " + key + "orderId: " + orderCreated.getId() + " - processedById: " + APPLICATION_ID);
    }
}
