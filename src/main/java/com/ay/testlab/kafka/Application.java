package com.ay.testlab.kafka;

import com.ay.testlab.kafka.batchlistener.BatchMessageConsumingExample;
import com.ay.testlab.kafka.multipartition.MultiPartitionMessagingExample;
import com.ay.testlab.kafka.simplemessage.SimpleKafkaMessagingExample;
import com.ay.testlab.kafka.streamapi.KafkaStreamExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private SimpleKafkaMessagingExample simpleKafkaMessagingExample;

    @Autowired
    private MultiPartitionMessagingExample multiPartitionMessagingExample;

    @Autowired
    private BatchMessageConsumingExample batchMessageConsumingExample;

    @Autowired
    private KafkaStreamExample kafkaStreamExample;

    @Bean
    @Profile("!test")
    public CommandLineRunner batchMessageConsumerRunner() {
        return args -> {
            //Just comment out the examples to run
            simpleKafkaMessagingExample.execute();
            //multiPartitionMessagingExample.execute();
            //batchMessageConsumingExample.execute();
            //kafkaStreamExample.execute();
        };
    }
}