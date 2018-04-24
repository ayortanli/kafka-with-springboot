package com.ay.testlab.kafka.multipartition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

public class MultiPartitionMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessageConsumer.class);

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", groupId = "multiPartitionWithSingleConsumer3Thread")
    public void receiver1a(@Payload String payload,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                        @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received consumer=1a group=multiPartitionWith2Consumer payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", groupId = "multiPartitionWithSingleConsumer3Thread")
    public void receiver1b(@Payload String payload,
                            @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                            @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received consumer=1b group=multiPartitionWith2Consumer payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", containerFactory = "kafkaListenerContainerFactoryWith3Consumer", groupId = "multiPartitionWithSingleConsumer3Thread")
    public void receive2(@Payload String payload,
                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                         @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received consumer=2 group=multiPartitionWithSingleConsumer3Thread payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }
}
