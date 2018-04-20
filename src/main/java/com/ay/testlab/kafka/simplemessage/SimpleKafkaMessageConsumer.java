package com.ay.testlab.kafka.simplemessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class SimpleKafkaMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaMessageConsumer.class);

    private String message = null;

    @KafkaListener(topics = "${kafka.topic.simpleMessageTopic}")
    public void receive(String payload) {
        LOGGER.info("Received payload='{}'", payload);
        this.message = payload;
    }

    //added for testing purpose
    public String message(){return this.message;}
}
