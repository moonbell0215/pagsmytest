package com.dht.pags.webservice.dispatcher.config;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * @author cloud.d
 */
@Configuration
@SuppressWarnings("unchecked")
public class KafkaStreamConfig {
    @Value("${spring.kafka.properties.bootstrap.servers}")
    private String BOOTSTRAP_SERVERS_CONFIG;

    @Value("${spring.kafka.properties.sasl.mechanism}")
    private String SASL_MECHANISM;
    @Value("${spring.kafka.properties.sasl.jaas.config}")
    private String SASL_JAAS_CONFIG;
    @Value("${spring.kafka.properties.security.protocol}")
    private String SECURITY_PROTOCOL_CONFIG;

    private static final String GROUP_ID_CONFIG = "webservice-dispatcher-consumer";
    private static final String CLIENT_ID_CONFIG = "webservice-dispatcher-consumer-client";
    /**
     * 监听已经处理好的交易事件
     */
    private static final String TOPIC_RECEIVE_EVENT = "wallet.transactionCreatedEvent";

    @Bean
    public KafkaSender<String, CreateTransactionCommand> kafkaSender() {
        final Map<String, Object> senderProps = new HashMap<>(3);
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);
        senderProps.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM);
        senderProps.put(SaslConfigs.SASL_JAAS_CONFIG, SASL_JAAS_CONFIG);
        senderProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG);

        return new DefaultKafkaSender(ProducerFactory.INSTANCE,
                SenderOptions.<String, CreateTransactionCommand>create(senderProps));
    }

    @Bean
    public KafkaReceiver<String, TransactionCreatedEvent> kafkaReceiver() {
        final Map<String, Object> receiverProps = new HashMap<>(6);
        receiverProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_CONFIG);
        receiverProps.put(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        receiverProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);
        receiverProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        receiverProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        receiverProps.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM);
        receiverProps.put(SaslConfigs.SASL_JAAS_CONFIG, SASL_JAAS_CONFIG);
        receiverProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG);
        receiverProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.dht.pags.wallet.domain");
        return new DefaultKafkaReceiver(ConsumerFactory.INSTANCE,
                ReceiverOptions.<String, TransactionCreatedEvent>create(receiverProps).subscription(Collections.singleton(TOPIC_RECEIVE_EVENT)));
    }
}