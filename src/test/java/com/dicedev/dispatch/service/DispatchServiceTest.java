package com.dicedev.dispatch.service;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dicedev.dispatch.message.OrderCreated;
import com.dicedev.dispatch.util.TestEventData;

public class DispatchServiceTest {

    private DispatchService service;

    @BeforeEach
    void setUp() {
        service = new DispatchService();
    }

    @Test
    void testProcess() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString()); 
        service.process(testEvent);
    }
}
