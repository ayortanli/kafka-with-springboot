package com.ay.testlab.kafka.streamapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class SimpleKafkaMessageProducerForStreaming {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaMessageProducerForStreaming.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String payload){
        LOGGER.info("Sending payload='{}' to topic='{}'", payload, topic);
        kafkaTemplate.send(topic, payload);
    }
}
