# kafka-with-springboot
Startup application for Kafka with Spring Boot

1. Kafka Setup:
- Download Kafka from <https://kafka.apache.org/downloads>
- Start Zookeper server with default configuration file (localhost:2181)
```
> ./bin/zookeeper-server-start.sh ./config/zookeeper.properties
```
- Start Kafka Server with default configuration file as single-broker cluster  (localhost:9092)
```
> ./bin/kafka-server-start.sh ./config/server.properties
```
- Create a test topic to use with the application.
```
> ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafkaTestTopic
```

(Windows users can use bash scripts under ./bin/windows folder)