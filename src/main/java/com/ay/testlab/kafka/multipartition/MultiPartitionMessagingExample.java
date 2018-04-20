package com.ay.testlab.kafka.multipartition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * MultiPartitionMessaging uses Spring Boot CommandLineRunner to send 300 consecutive messages to 10 partition of Kafka server
 */
@Component
public class MultiPartitionMessagingExample {

    @Autowired
    private MultiPartitionMessageProducer sender;

    @Value("${kafka.topic.multiPartitionTopic}")
    private String topicName;


    @Bean
    @Profile("!test")
    public CommandLineRunner multiPartitionMessageRunner() {
        return args -> {
            for (int i = 0; i < 30; ++i) {
                for(int partitionKey = 1; partitionKey<=10; ++ partitionKey) {
                    sender.send(topicName, "key"+partitionKey, "MultiPartitionMessaging - Message No = " +partitionKey+"-"+i);
                }
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
