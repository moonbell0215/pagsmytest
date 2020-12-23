package com.dht.pags.webservice.dispatcher;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RestController
public class WebController {

    private KafkaSender<String, String> kafkaProducer;
    private ObjectMapper objectMapper;
    private Flux<ReceiverRecord<String, TransactionCreatedEvent>> transactionCreatedEventFlux;

    public WebController() {
        final Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        final SenderOptions<String, String> producerOptions = SenderOptions.create(producerProps);
        kafkaProducer = KafkaSender.create(producerOptions);

        final Map<String, TransactionCreatedEvent> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "webservice-dispatcher");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        ReceiverOptions<String, TransactionCreatedEvent> consumerOptions = ReceiverOptions.create(consumerProps)
                .subscription(Collections.singleton("wallet.transactionCreatedEvent"));
        transactionCreatedEventFlux = KafkaReceiver.create(consumerOptions).receive();

        objectMapper = new ObjectMapper();
    }

    @PostMapping(value = "/transaction")
    public Mono<TransactionCreatedEvent> doTransaction(@RequestBody CreateTransactionCommand command) throws JsonProcessingException {
        final String transactionIdFromCommand = command.getTransactionId();
        SenderRecord<String, String, Integer> message = SenderRecord.create
                (new ProducerRecord<>("wallet.createTransactionCommand", objectMapper.writeValueAsString(command)), 1);
        kafkaProducer.send(Mono.just(message));
        Mono<ReceiverRecord<String, TransactionCreatedEvent>> mono = transactionCreatedEventFlux.filter(x -> x.value().getId().equals(transactionIdFromCommand)).next();
        return mono;
    }

}
