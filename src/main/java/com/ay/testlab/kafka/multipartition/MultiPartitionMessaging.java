package com.ay.testlab.kafka.multipartition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * MultiPartitionMessaging uses Spring Boot CommandLineRunner to send 100 consecutive messages to Kafka server
 */
@Component
public class MultiPartitionMessaging {

    @Autowired
    private MultiPartitionMessageProducer sender;

    @Value("${kafka.topic.multiPartitionTopic}")
    private String topicName;


    @Bean
    public CommandLineRunner multiPartitionMessageRunner() {
        return args -> {
            for (int i = 0; i < 100; ++i) {
                sender.send(topicName, "MultiPartitionMessaging - Message No : " + i);
            }
        };
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
