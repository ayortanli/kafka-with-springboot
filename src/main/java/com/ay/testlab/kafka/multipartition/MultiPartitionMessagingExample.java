package com.ay.testlab.kafka.multipartition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * MultiPartitionMessaging uses Spring Boot CommandLineRunner to send 300 consecutive messages to 10 partition of Kafka server
 */
@Component
public class MultiPartitionMessagingExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessagingExample.class);

    @Autowired
    private MultiPartitionMessageProducer sender;

    @Value("${kafka.topic.multiPartitionTopic}")
    private String topicName;

    public void execute() {
        LOGGER.info("MultiPartitionMessagingExample is executing...");
        for (int i = 0; i < 30; ++i) {
            for(int partitionKey = 1; partitionKey<=10; ++ partitionKey) {
                sender.send(topicName, "key"+partitionKey, "MultiPartitionMessaging - Message No = " +partitionKey+"-"+i);
            }
        }
    }

    @Bean
    public MultiPartitionMessageProducer multiPartitionMessageProducer(){
        return new MultiPartitionMessageProducer();
    }

    @Bean
    public MultiPartitionMessageConsumer multiPartitionMessageConsumer(){
        return new MultiPartitionMessageConsumer();
    }
}
