package com.ay.testlab.kafka.multipartition;

import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

public class MultiPartitionMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessageConsumer.class);

    //https://memorynotfound.com/spring-kafka-batch-listener-example/
    //https://docs.spring.io/spring-kafka/api/org/springframework/kafka/support/KafkaHeaders.html#CONSUMER
    //Headerda aşağıdaki bilgileri de yazdıralım...

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", containerFactory = "KafkaListenerContainerFactoryWith6Consumer", groupId = "multiPartitionWith6Consumer")
    public void receive(@Payload String payload, @Header(KafkaHeaders.CONSUMER)Consumer<String,String> consumer) {
        LOGGER.info("Received payload='{}' from topic-partition@offset:consumerId='{}'", payload, consumer);
    }
}
