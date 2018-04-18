# kafka-with-springboot
Startup application for Kafka with Spring Boot

This project is aimed for starting Kafka based project much faster by providing simple example applications for different scenarios. It may also be used as a tutorial for those who likes learning by playing with codes. :) 

### 1. Kafka Setup:
- Download Kafka from <https://kafka.apache.org/downloads>
- Start Zookeper server with default configuration file (localhost:2181)
```bash
> ./bin/zookeeper-server-start.sh ./config/zookeeper.properties
```
- Start Kafka Server with default configuration file as single-broker cluster  (localhost:9092)
```bash
> ./bin/kafka-server-start.sh ./config/server.properties
```
- Create a test topic to use with the application.
```bash
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafkaTestTopic
```

(Windows users can use bash scripts under ./bin/windows folder)

### 2. Application Configuration Notes
- For each example, Spring Boot's CommandLineRunner interface is implemented. 
*"CommandLineRunnner interface used to indicate that a bean should run when it is contained within a SpringApplication. Multiple CommandLineRunner beans can be defined within the same application context."*  All examples are configured to run simultaneously. In order to see output of a specific example more clear, disable the bean annotation of runner methods of other examples.
- Spring boot can inject properties from YAML files by default when they are defined in resource/application.yml. Properties like Kafka initial lookup server address, topic names, and etc. are defined there.
- KafkaProducerConfig and KafkaConsumerConfig classes contains base configurations for Kafka.

### 3. Simple Kafka Messaging Example  
In the simplemessage package, a message consume/produce example is implemented. In SimpleKafkaMessaging class, we send 100 consecutive messages to Kafka with our producer. Then these messages are consumed by our consumer which subscribes to Kafka server during initialization. Producer and Consumer classes are given below.  
```java
public class SimpleKafkaMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaMessageProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String payload){
        LOGGER.info("Sending payload='{}' to topic='{}'", payload, topic);
        kafkaTemplate.send(topic, payload);
    }
}
```
For sending messages, KafkaTemplate is used. KafkaTemplate is a wrapper for Producer api. It simplifies sending data to kafka topics.

```java
public class SimpleKafkaMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaMessageConsumer.class);

    @KafkaListener(topics = "${kafka.topic.simpleMessageTopic}")
    public void receive(String payload) {
        LOGGER.info("Received payload='{}'", payload);
    }
}
```
For consuming messages, we just use @KafkaListener annotation with the suitable topic name. @KafkaListener annotation defines consumer methods for Kafka topics. Note that, We also use @EnableKafka annotation in KafkaConsumerConfig configuration class. It enables auto detection of @KafkaListener annotations on any Spring-managed bean in the container. 

### 4. Multi-Partition Messaging Example
This example will demonstrate usage of Kafka with multi-partitioned topic with two consumer groups.

For this example, we first create a new topic "kafkaMultiPartitionTopic" with 3 partitions and also add it to our application.yml file under resource folder.
```bash
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic kafkaMultiPartitionTopic
```
Now we have a new topic with 3 partitions. In this example, we make a little change to send method in producer class to control partition selection logic;

Here, we request another parameter called *key*. Kafka uses this parameter to determine which partition is assigned for message. Kafka guarantees that messages with same key value will be assigned to the same partition.

For this example we also add success and failure callbacks. These callbacks return valuable information after message is retrieved by Kafka server.
```java
public class MultiPartitionMessageProducer {
    //...
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
    //...
}
```
Next, lets define our consumers. This time, we'll create two consumer groups. One with 3 members and other one has 6 consumers. The important point here is that, if you want to consume messages in a partition in order, you should at most provide same number of consumers with partition numbers. In our example, our consumer group with 3 consumer will consume messages in partitions in order. Because Kafka will assign consumers to partitions one-by-one. On the other hand, second consumer group (with 6 consumer) will lose order while consuming. Since, some partitions will be assigned with more than one consumer (Most probably 2 consumer for each partition).

While defining our consumers, we set consumer group name by *groupId* parameter. Number of consumers are defined in *containerFactory* by setting concurreny level. For this example, different containerFactory beans with different concurrency are created in KafkaConsumerConfig class.:
```java
public class MultiPartitionMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessageConsumer.class);

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", containerFactory = "kafkaListenerContainerFactoryWith6Consumer", groupId = "multiPartitionWith6Consumer")
    public void receive1(@Payload String payload,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                        @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received group=multiPartitionWith6Consumer payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", containerFactory = "kafkaListenerContainerFactoryWith3Consumer", groupId = "multiPartitionWith3Consumer")
    public void receive2(@Payload String payload,
                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                         @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received group=multiPartitionWith3Consumer payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }
}
```

### 5. Batch Message Consuming Example
This example will demonstrate usage of Kafka in batch mode. That is; consumers consume messages not one by one but as a group. 

Lets first create a new topic "kafkaBatchConsumerTopic" and again add it to our application.yml file under resource folder.
```bash
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafkaBatchConsumerTopic
```
