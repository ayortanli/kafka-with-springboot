package com.ay.testlab.kafka.batchlistener;

import com.ay.testlab.kafka.simplemessage.SimpleKafkaMessagingExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BatchMessageConsumingExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchMessageConsumingExample.class);

    @Autowired
    private MessageProducerForBatchListener sender;

    @Value("${kafka.topic.batchConsumerTopic}")
    private String topicName;

    public void execute() {
        LOGGER.info("BatchMessageConsumingExample is executing...");
        for (int i = 0; i < 100; ++i) {
            sender.send(topicName, "BatchMessageConsuming - Message No = " + i);
        }
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
