package com.ay.testlab.kafka.simplemessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * SimpleKafkaMessaging uses Spring Boot CommandLineRunner to send 100 consecutive messages to Kafka server
 */
@Component
public class SimpleKafkaMessagingExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaMessagingExample.class);

    @Autowired
    private SimpleKafkaMessageProducer sender;

    @Value("${kafka.topic.simpleMessageTopic}")
    private String topicName;

    public void execute() {
        LOGGER.info("SimpleKafkaMessagingExample is executing...");
        for (int i = 0; i < 100; ++i) {
            sender.send(topicName, "SimpleKafkaMessaging - Message No = " + i);
        }
    }

    @Bean
    public SimpleKafkaMessageProducer simpleKafkaMessageProducer(){
        return new SimpleKafkaMessageProducer();
    }

    @Bean
    public SimpleKafkaMessageConsumer simpleKafkaMessageConsumer(){
        return new SimpleKafkaMessageConsumer();
    }
}
