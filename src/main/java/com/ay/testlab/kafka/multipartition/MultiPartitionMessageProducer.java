package com.ay.testlab.kafka.multipartition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

public class MultiPartitionMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessageProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String key, String payload){
        LOGGER.info("Sending payload='{}' to topic='{}' with key='{}'", payload, topic, key);
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key ,payload);
        SuccessCallback<SendResult<String,String>> successCallback = sendResult -> {
            LOGGER.info("Sent payload='{}' with key='{}' to topic-partition@offset='{}'", payload, key, sendResult.getRecordMetadata().toString());
        };
        FailureCallback failureCallback = throwable -> {
            LOGGER.info("Sending payload='{}' to topic='{}' with key='{}' failed!!!", payload, topic, key);
        };
        future.addCallback(successCallback, failureCallback);
    }
}
