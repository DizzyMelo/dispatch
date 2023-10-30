package com.dicedev.dispatch.integration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.dicedev.dispatch.DispatchConfiguration;
import com.dicedev.dispatch.message.DispatchPreparing;
import com.dicedev.dispatch.message.OrderCreated;
import com.dicedev.dispatch.message.OrderDispatched;
import com.dicedev.dispatch.util.TestEventData;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = {DispatchConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true)
public class OrderDispatchIntegrationTest {

    private final static String ORDER_CREATED_TOPIC = "order.created";
    private final static String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private final static String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    private KafkaTestListener testListener;

    @Configuration
    static class TestConfig {

        @Bean
        KafkaTestListener testListener() {
            return new KafkaTestListener();
        }
    }


    public static class KafkaTestListener {
        AtomicInteger dispatchPreparingCounter = new AtomicInteger(0);
        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);


        @KafkaListener(groupId = "kafkaIntegrationTest", topics = DISPATCH_TRACKING_TOPIC)
        void receiveDispatchPreparing(@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchPreparing payload) {
            log.debug("Event received: key:" + key + " -payload: " + payload);

            Assert.notNull(key, "not null");
            Assert.notNull(payload, "not null");

            dispatchPreparingCounter.incrementAndGet();
        }


        @KafkaListener(groupId = "kafkaIntegrationTest", topics = ORDER_DISPATCHED_TOPIC)
        void receiveDispatchPreparing(@Header (KafkaHeaders.RECEIVED_KEY) String key,  @Payload OrderDispatched payload) {
            log.debug("Event received: " + payload);

            Assert.notNull(key, "not null");
            Assert.notNull(payload, "not null");
            
            orderDispatchedCounter.incrementAndGet();
        }

    }

    @BeforeEach
    void setUp() {
        testListener.dispatchPreparingCounter.set(0);
        testListener.orderDispatchedCounter.set(0);

        endpointRegistry.getListenerContainers().stream().forEach( container -> 
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
    }
    
    @Test
    public void testOrderDispatchFlow() throws Exception {
        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), "my-item");
        String key = UUID.randomUUID().toString();
        sendMessage(ORDER_CREATED_TOPIC, key, orderCreated);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
            .until(testListener.dispatchPreparingCounter::get, equalTo(1));

            
        await().atMost(1, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
            .until(testListener.orderDispatchedCounter::get, equalTo(1));
    }

    private void sendMessage(String topic, String key, Object data) throws Exception {
        kafkaTemplate.send(MessageBuilder
            .withPayload(data)
            .setHeader(KafkaHeaders.KEY, key)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .build()).get();
    }


}
