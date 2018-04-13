package com.ay.testlab.kafka.multipartition;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

public class MultiPartitionMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessageConsumer.class);

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", containerFactory = "KafkaListenerContainerFactoryWith6Consumer", groupId = "multiPartitionWith6Consumer")
    public void receive(@Payload String payload,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                        @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }
}
