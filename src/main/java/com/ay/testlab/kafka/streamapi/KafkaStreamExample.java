package com.ay.testlab.kafka.streamapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamExample.class);

    @Autowired
    private SimpleKafkaMessageProducerForStreaming sender;

    @Value("${kafka.topic.streamRawDataTopic}")
    private String streamRawDataTopic;

    public void execute() {
        LOGGER.info("KafkaStreamExample is executing...");
        for (int i = 0; i < 100; ++i) {
            sender.send(streamRawDataTopic, "SimpleKafkaMessaging - Message No = " + i);
        }
    }


    @Bean
    public SimpleKafkaMessageProducerForStreaming simpleKafkaMessageProducerForStreaming(){
        return new SimpleKafkaMessageProducerForStreaming();
    }

    @Bean
    public SimpleKafkaMessageConsumerForStreaming simpleKafkaMessageConsumerForStreaming(){ return new SimpleKafkaMessageConsumerForStreaming();
    }
}
