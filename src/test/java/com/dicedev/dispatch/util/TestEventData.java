package com.dicedev.dispatch.util;

import java.util.UUID;

import com.dicedev.dispatch.message.DispatchPreparing;
import com.dicedev.dispatch.message.OrderCreated;

public class TestEventData {
    public static OrderCreated buildOrderCreatedEvent(UUID id, String item) {
        return OrderCreated.builder()
                .id(id)
                .item(item)
                .build();
    }

    public static DispatchPreparing builDispatchPreparingEvent(UUID id) {
        return DispatchPreparing.builder()
                .id(id)
                .build();
    }
}
