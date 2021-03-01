package com.ay.testlab.kafka;

import com.ay.testlab.kafka.simplemessage.SimpleKafkaMessageConsumer;
import com.ay.testlab.kafka.simplemessage.SimpleKafkaMessageProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka
@ActiveProfiles("test")
public class SimpleKafkaMessageTest {

    @Value("${kafka.topic.simpleMessageTopic}")
    private String topicName;

    @Autowired
    private SimpleKafkaMessageProducer sender;

    @Autowired
    private SimpleKafkaMessageConsumer consumer;

    @Test
    public void testSendReceive(EmbeddedKafkaBroker broker) throws Exception {
        sender.send(topicName, "test Message");
        TimeUnit.SECONDS.sleep(1);
        Assertions.assertEquals("test Message", consumer.message());
    }
}
