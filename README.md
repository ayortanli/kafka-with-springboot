# kafka-with-springboot
Startup application for Kafka with Spring Boot

##1. Kafka Setup:
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

##2. Application Configuration Notes
- For each example, Spring Boot's CommandLineRunner interface is implemented. 
*"CommandLineRunnner interface used to indicate that a bean should run when it is contained within a SpringApplication. Multiple CommandLineRunner beans can be defined within the same application context."*   
- Spring boot can inject properties from YAML files by default when they are defined in resource/application.yml. Properties like Kafka initial lookup server address, topic names, and etc. are defined there.
- KafkaProducerConfig and KafkaConsumerConfig classes contains base configurations for Kafka.

##3. Simple Kafka Messaging Example  
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

##4. Multi-Partition Messaging Example
This example will demonstrate usage of Kafka with multi-partitioned topic with two consumer groups.

For this example we first create a new topic "kafkaMultiPartitionTopic" with 3 partitions and also add it to our application.yml file under resource folder.
```bash
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic kafkaMultiPartitionTopic
```