package com.ay.testlab.kafka.batchlistener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;

public class BatchMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchMessageConsumer.class);

    private int consumerTriggerCount = 0;

    @KafkaListener(topics = "${kafka.topic.batchConsumerTopic}", containerFactory = "kafkaListenerContainerFactoryForBatchConsumer", groupId = "batchConsumer")
    public void receive(@Payload List<String> payloads,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Long> partitionIds,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        LOGGER.info("Received group=batchConsumer with batch group data: ");
        consumerTriggerCount++;
        for (int i = 0; i< payloads.size(); ++i) {
            LOGGER.info("---------------- consumerTriggerCount='{}' payload='{}' from partitionId@offset='{}'", consumerTriggerCount, payloads.get(i), partitionIds.get(i)+"@"+offsets.get(i));
        }

    }
}
