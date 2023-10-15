package com.dicedev.dispatch.handler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dicedev.dispatch.message.OrderCreated;
import com.dicedev.dispatch.service.DispatchService;
import com.dicedev.dispatch.util.TestEventData;

public class OrderCreatedHandlerTest {

    private OrderCreatedHandler orderCreatedHandler;
    private DispatchService dispatchServiceMock;

    @BeforeEach
    void setUp() {
        dispatchServiceMock = mock(DispatchService.class);
        orderCreatedHandler = new OrderCreatedHandler(dispatchServiceMock);
    }

    @Test
    void listen_Success() throws Exception{
        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString());
        orderCreatedHandler.listen(orderCreated);
        verify(dispatchServiceMock, times(1)).process(orderCreated);;
    }

    @Test
    void listen_ServiceThrowsException() throws Exception{
        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString());
        doThrow(new RuntimeException()).when(dispatchServiceMock).process(orderCreated);

        orderCreatedHandler.listen(orderCreated);
        verify(dispatchServiceMock, times(1)).process(orderCreated);;
    }
}
