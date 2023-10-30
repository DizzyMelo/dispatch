package com.dicedev.dispatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import com.dicedev.dispatch.message.DispatchPreparing;
import com.dicedev.dispatch.message.OrderCreated;
import com.dicedev.dispatch.message.OrderDispatched;
import com.dicedev.dispatch.util.TestEventData;

public class DispatchServiceTest {

    private DispatchService service;
    private KafkaTemplate<String, Object> kafkaProducerMock;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        service = new DispatchService(kafkaProducerMock);
    }

    @Test
    void testProcess_Success() throws Exception {
        String key = UUID.randomUUID().toString();
        
        when(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(), anyString(), any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));
        
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString()); 
        service.process(key, testEvent);
        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
    }

    
    @Test
    void testProcess_DispatchPrepatingThrowsException() throws Exception {
        String key = UUID.randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString()); 
        doThrow(new RuntimeException("Producer failure")).when(kafkaProducerMock).send(eq("dispatch.tracking"), anyString(), any(DispatchPreparing.class));
        
        Exception exception = assertThrows(RuntimeException.class, () -> service.process(key, testEvent));
        
        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verifyNoMoreInteractions(kafkaProducerMock);
        assertEquals(exception.getMessage(), "Producer failure");
    }

    @Test
    void testProcess_OrderDispatchThrowsException() throws Exception {
        String key = UUID.randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString()); 
        when(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        doThrow(new RuntimeException("Producer failure")).when(kafkaProducerMock).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
        
        Exception exception = assertThrows(RuntimeException.class, () -> service.process(key, testEvent));
        
        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
        assertEquals(exception.getMessage(), "Producer failure");
    }
}
