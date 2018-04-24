package com.ay.testlab.kafka.batchlistener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//@Component
public class BatchMessageConsumingExample {

    @Autowired
    private MessageProducerForBatchListener sender;

    @Value("${kafka.topic.batchConsumerTopic}")
    private String topicName;

    @Bean
    @Profile("!test")
    public CommandLineRunner batchMessageConsumerRunner() {
        return args -> {
            for (int i = 0; i < 100; ++i) {
                sender.send(topicName, "BatchMessageConsuming - Message No = " + i);
            }
        };
    }

    @Bean
    public MessageProducerForBatchListener messageProducer(){
        return new MessageProducerForBatchListener();
    }

    @Bean
    public BatchMessageConsumer batchMessageConsumer(){
        return new BatchMessageConsumer();
    }
}
