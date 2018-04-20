package com.ay.testlab.kafka;

import com.ay.testlab.kafka.simplemessage.SimpleKafkaMessageConsumer;
import com.ay.testlab.kafka.simplemessage.SimpleKafkaMessageProducer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
public class SimpleKafkaMessageTest {

    @Value("${kafka.topic.simpleMessageTopic}")
    private String topicName;

    @Autowired
    private SimpleKafkaMessageProducer sender;

    @Autowired
    private SimpleKafkaMessageConsumer consumer;

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1,false, "testingTopic");

    @Test
    public void testSendReceive() throws Exception {
        sender.send(topicName, "test Message");
        TimeUnit.SECONDS.sleep(1);
        Assert.assertEquals("test Message", consumer.message());
    }
}
