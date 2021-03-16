# kafka-with-springboot
Demonstrations for Kafka with Spring Boot

This project is created for starting Kafka based project much faster by providing simple example applications for different scenarios. It may also be used as a tutorial for those who likes learning by playing with codes. :)  

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
(Windows users can use bash scripts under ./bin/windows folder)

### 2. Application Configuration Notes
- For each example, just activate related commented lines in *Application.java*. 
- Spring boot can inject properties from YAML files by default when they are defined in resource/application.yml. Properties like Kafka initial lookup server address, topic names, and etc. are defined there.
- KafkaProducerConfig and KafkaConsumerConfig classes contains base configurations for Kafka.
- There is also a dummy rest controller, if you wish to play with Kafka in rest calls.

### 3. Simple Kafka Messaging Example
First Create a test topic to use with the example.
```bash
> ./bin/kafka-topics.sh --create --topic kafkaSimpleMessageTopic --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1
```

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
> ./bin/kafka-topics.sh --create --topic kafkaMultiPartitionTopic --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3
```
Now we have a new topic with 3 partitions. In this example, we'll make a little change to our message sending method in producer class to control partition selection logic;

Here, *kafkaTemplate* requires another parameter called *key*. Kafka uses this parameter as an input to its hash function to determine which partition is assigned for message. Kafka guarantees that messages with the same key value will be assigned to the same partition.

We also add success and failure callbacks here for debugging purpose. These callbacks return valuable information after message is retrieved by Kafka server.
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
Next, lets define our consumers. This time, we'll create two consumer groups. 

First consumer group consists of two different consumers (receiver1a and receiver1b). Since we have 3 partitions, Kafka will assign two partitions to one consumer and one partition to the other one. 

Second consumer group contains 6 consumer threads which'll call same listener method (receive2). This time, we create consumers by setting concurrency level of ListenerContainer. As a result, all our consumers'll call same listener method with multi-thread way. I have used concurrency just to see how different it'' work. It differs only in multi-thread nature and all consumers works in the same listener container. 

The important point in this example is that, if the number of consumers are less than number of partitions then some partitions will be assigned to the same consumers. If there are equal number of partitions and consumers then each consumer will handle one partition. Finally, if consumers are high in number then some of them will be idle and only be there for high availability. 

While defining our consumers, we set consumer group name by *groupId* parameter. Number of consumers are defined in *containerFactory* by setting concurreny level. For this example, different containerFactory beans with different concurrency are created in KafkaConsumerConfig class.:
```java
public class MultiPartitionMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartitionMessageConsumer.class);

    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", groupId = "multiPartitionWith2Consumer")
    public void receiver1a(@Payload String payload,
                            @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                            @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received consumer=1a group=multiPartitionWith2Consumer payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }
    
    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", groupId = "multiPartitionWith2Consumer")
    public void receiver1b(@Payload String payload,
                            @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                            @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received consumer=1b group=multiPartitionWith2Consumer payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }
    
    @KafkaListener(topics = "${kafka.topic.multiPartitionTopic}", containerFactory = "kafkaListenerContainerFactoryWith6Consumer", groupId = "multiPartitionWithSingleConsumer6Thread")
    public void receive2(@Payload String payload,
                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID)Long partitionId,
                         @Header(KafkaHeaders.OFFSET)Long offset) {
        LOGGER.info("Received consumer=2 group=multiPartitionWithSingleConsumer6Thread payload='{}' from partitionId@offset='{}'", payload, partitionId+"@"+offset);
    }
}
```

### 5. Batch Message Consuming Example
This example will demonstrate usage of Kafka in batch mode. That is; consumers consume messages not one by one but as a group. 

Lets first create a new topic "kafkaBatchConsumerTopic" and again add it to our application.yml file under resource folder.
```bash
> ./bin/kafka-topics.sh --create --topic kafkaBatchConsumerTopic --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1
```

I will use a simple message producer in this example, what we will change here is only consumer part. Our consumer should be configured and implemented in a way that it can handle multiple message at a time. First lets take a look at configuration part.

In KafkaConsumerConfig class, we create a new KafkaListenerContainerFactory which can create listeners for batch operations. This is done by *batchListener* property of our factory as follows:

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    
    //....
    
    @Bean(name = "kafkaListenerContainerFactoryForBatchConsumer")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactoryForBatchConsumer() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(1);
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
    
    //....
}
```

Now our consumer can receive more than one records. The number of records received is dynamically calculated in our configuration. It is also possible to set an upper limit for that. This can be done by setting *MAX_POLL_RECORDS_CONFIG* property on *ConsumerConfig* configuration. 

After than, consumer implementation should only be updated to receive list of messages as parameters as follows:

```java

public class BatchMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchMessageConsumer.class);

    @KafkaListener(topics = "${kafka.topic.batchConsumerTopic}", containerFactory = "kafkaListenerContainerFactoryForBatchConsumer", groupId = "batchConsumer")
    public void receive(@Payload List<String> payloads,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Long> partitionIds,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        LOGGER.info("Received group=batchConsumer with batch group data: ");
        for (int i = 0; i< payloads.size(); ++i) {
            LOGGER.info("---------------- payload='{}' from partitionId@offset='{}'", payloads.get(i), partitionIds.get(i)+"@"+offsets.get(i));
        }

    }
}

```

## 6. Kafka Streams Api Example

For using Kafka Streams, we need to first add kafka-streams library to our project.   
Note: kafka-streams doesn't work on windows platform because of a bug in 2.6.1 release. Either use 2.6.0 or wait for 2.6.2...
```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>2.6.1</version>
</dependency>
```
For this example, we will create a scenario where producer will send message to a topic. Than our kafka stream code will read streams of data, process it and send it to another topic. Finally our consumer will get processed data and do its job.

Lets start with creation of two topics one for raw data and another one for processed data.

```bash
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic kafkaStreamRawDataTopic
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic kafkaStreamProcessedDataTopic
```

Then we will create a simple producer that sends data to *kafkaStreamRawDataTopic* and a simple consumer that reads data from *kafkaStreamProcessedDataTopic*. 

Now we can talk about stream processing code block which; in our case, is *SimpleKafkaStream.java*. In our stream class we should define *@EnableKafkaStream* annotation with *@Configuration* annotation by which kafka-streams can declare some beans like StreamBuilder in the application context automatically.

After then, we define config method for connecting our stream code to Kafka and implement our stream processing method. In this example we just reverse the string stream data and push it to another topic. As final note; in our configuration bean, we set bean name to *DEFAULT_STREAMS_CONFIG_BEAN_NAME*. By this way, Spring Boot'll inject default StreamBuilder. Otherwise, we should also create a StreamBuilder bean along with our configuration bean.  

```java
@Configuration
@EnableKafka
@EnableKafkaStreams
public class SimpleKafkaStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaStream.class);

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.streamRawDataTopic}")
    private String rawDataTopic;

    @Value("${kafka.topic.streamProcessedDataTopic}")
    private String processedDataTopic;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kafkaStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "simpleKafkaStream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        return new StreamsConfig(props);
    }

    @Bean
    public KStream<String, String> kafkaStream(StreamsBuilder kStreamBuilder) {
        KStream<String, String> stream = kStreamBuilder.stream(rawDataTopic);

        //process messages (reverse order)
        stream.mapValues(messageValue -> {
            LOGGER.info("Stream:SimpleKafkaStream processing payloads='{}'", messageValue);
            return new StringBuilder(messageValue).reverse().toString();
        }).to(processedDataTopic);

        LOGGER.info("Stream started here...");
        return stream;
    }
}

```

## 7. Kafka Connect Api Example
Kafka connect api provides data read/write interfaces between different data sources (file, database, cloud, etc.) and Kafka topics. It can be used just by injecting configuration files to Kafka Server. It is also possible to use it by implementing our own Kafka connect api based applications. We'll try both of them.
#### Connect api with configuration files
For configuration based example, we will work on file based connectors since no setup required. Default configuration files under the Kafka config folder can be used as base configuration. In our example, we'll read from file to a topic then process messages with our kafka stream that we have implemented in the previous example. Finally, our second connector will write processed messages into another file.

Let's start with configuring our data reader (source connector).

connect-file-source.properties
```properties
# Unique name for the connector. Attempting to register again with the same name will fail.
name=local-file-source
# The Java class for the connector
connector.class=FileStreamSource
# The maximum number of tasks that should be created for this connector. The connector may create fewer tasks if it cannot achieve this level of parallelism.
tasks.max=1
# source filename
file=sourceData.txt
# topic where read data will be sent
topic=kafkaStreamRawDataTopic
# messages can also be modified with transformations like below: (this transform adds a field and its value to each message)
# transforms=InsertSource
# transforms.InsertSource.type=org.apache.kafka.connect.transforms.InsertField$Value
# transforms.InsertSource.static.field=data_source
# transforms.InsertSource.static.value=test-file-source
```
After reading data from file and sending messages to *kafkaStreamRawDataTopic*, Kafka stream application'll process messages and send them to *kafkaStreamProcessedDataTopic*. Then lets configure our data writer (sink connector)

connect-file-sink.properties
```properties
# Unique name for the connector. Attempting to register again with the same name will fail.
name=local-file-sink
# The Java class for the connector
connector.class=FileStreamSink
# The maximum number of tasks that should be created for this connector. The connector may create fewer tasks if it cannot achieve this level of parallelism.
tasks.max=1
# source filename
file=sinkData.txt
# topic where messages are retrieved in order to write
topic=kafkaStreamProcessedDataTopic
```
Now our sink connector read messages from topic *kafkaStreamProcessedDataTopic* and write them to the *sinkData.txt* file.

One more configuration file is required for defining environment properties like server address, converter types, etc.

connect-file-standalone.properties
```properties
bootstrap.servers=localhost:9092
# The converters specify the format of data in Kafka and how to translate it into Connect data. Every Connect user will
# need to configure these based on the format they want their data in when loaded from or stored into Kafka
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter
# Converter-specific settings can be passed in by prefixing the Converter's setting with the converter we want to apply
# it to
key.converter.schemas.enable=true
value.converter.schemas.enable=true
```

Finally, we can start our connectors by injecting configuration files to Kafka. Note that, we can inject any number of connectors with the following command 
```bash
> ./bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-source.properties config/connect-file-sink.properties
```
(Don't forget to add some data to *sourceData.txt file* to see the result)

#### Connect api with programmatic implementation
Same configuration can also be implemented programmatically with Kafka Connect api. There are two group of classes to implement: *SourceConnector* / *SinkConnector* and *SourceTask* / *SinkTask*. These can be used to implement our own connectors. 

"Connectors do not perform any data copying themselves: their configuration describes the data to be copied, and the Connector is responsible for breaking that job into a set of Tasks that can be distributed to workers. These Tasks also come in two corresponding flavors: SourceTask and SinkTask. With an assignment in hand, each Task must copy its subset of the data to or from Kafka..." (from documentation)

There is currently no Spring Boot integration. Also writing our own connectors seems to be a little bit complex according to configuration based ones. Therefore, I am not planning to implement an example for now. Those who want to try can use <https://kafka.apache.org/documentation/#connect> 


### - Extra: Unit Testing with Embedded Kafka Server

*spring-kafka-test* library includes an embedded kafka server which can be used in testing our kafka dependent application logic. In order to use it, first we should add testing libraries (spring-boot-starter-test and spring-kafka-test) to maven pom file.

Next we'll create a new application.yml file under test resources folder for use with test cases. We'll set our server address and also test topic name here. Note that, embedded kafka server is started on a random port. Therefore, we are using the *spring.embedded.kafka.brokers* property as server address. This property is set by the KafkaEmbedded class which we will use to start embedded kafka server.
```yaml
kafka:
  bootstrap-servers: ${spring.embedded.kafka.brokers}
  topic:
    simpleMessageTopic: testingTopic
```

KafkaEmbedded class is annotated with @ClassRule. It starts Kafka server on a random port before test cases starts. It gets three arguments. These are number of servers, whether controlled server shutdown is required or not, and topic names on servers. Our simple test case is as follows: 

```java
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
```
### - Extra Kafka Monitoring Tools (Open Source)
- [Kafka Manager](https://github.com/yahoo/CMAK)  - The most known one, but seems not maintained for a while.
- [Kafdrop](https://github.com/obsidiandynamics/kafdrop) - The next popular one.
