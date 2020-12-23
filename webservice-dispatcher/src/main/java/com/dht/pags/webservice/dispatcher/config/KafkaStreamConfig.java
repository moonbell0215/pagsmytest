package com.dht.pags.webservice.dispatcher.config;

import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.internals.DefaultKafkaSender;
import reactor.kafka.sender.internals.ProducerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author cloud.d
 */
@Configuration
public class KafkaStreamConfig {
    private static final String GROUP_ID_CONFIG = "webservice-dispatcher";
    /** 这个ClientID作用是什么 */
    private static final String CLIENT_ID_CONFIG = UUID.randomUUID().toString();
    private static final String BOOTSTRAP_SERVERS_CONFIG = "localhost:9092";
    /**
     * 监听已经处理好的交易事件
     */
    private static final String TOPIC_RECEIVE_EVENT = "wallet.transactionCreatedEvent";

    @Bean
    public KafkaSender<String, String> kafkaSender() {
        final Map<String, Object> senderProps = new HashMap<>(3);
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);
        return new DefaultKafkaSender(ProducerFactory.INSTANCE, SenderOptions.create(senderProps));
    }

    @Bean
    public KafkaReceiver<Object, TransactionCreatedEvent> kafkaReceiver() {
        final Map<String, Object> receiverProps = new HashMap<>(5);
        receiverProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_CONFIG);
        receiverProps.put(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        receiverProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);
        receiverProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        receiverProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaReceiver(ConsumerFactory.INSTANCE, ReceiverOptions.create(receiverProps).subscription(Collections.singleton(TOPIC_RECEIVE_EVENT)));
    }
}