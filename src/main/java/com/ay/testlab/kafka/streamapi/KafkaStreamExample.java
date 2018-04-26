package com.ay.testlab.kafka.streamapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamExample {

    @Autowired
    private SimpleKafkaMessageProducer sender;

    @Value("${kafka.topic.streamRawDataTopic}")
    private String streamRawDataTopic;

    @Bean
    @Profile("!test")
    public CommandLineRunner KafkaStreamMessageRunner() {
        return args -> {
            for (int i = 0; i < 100; ++i) {
                sender.send(streamRawDataTopic, "SimpleKafkaMessaging - Message No = " + i);
            }
        };
    }


    @Bean
    public SimpleKafkaMessageProducer simpleKafkaMessageProducer(){
        return new SimpleKafkaMessageProducer();
    }

    @Bean
    public SimpleKafkaMessageConsumer simpleKafkaMessageConsumer(){ return new SimpleKafkaMessageConsumer();
    }
}
