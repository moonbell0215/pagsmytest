package com.dht.pags.webservice.dispatcher;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import com.dht.pags.wallet.domain.TransactionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class WebController {
    private static Logger LOGGER = LoggerFactory.getLogger(WebController.class);

    private KafkaSender<String, CreateTransactionCommand> kafkaProducer;
    private ObjectMapper objectMapper;
    private Flux<ReceiverRecord<String, String>> transactionCreatedEventFlux;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS z dd MMM yyyy");
    private AtomicInteger sendCounter = new AtomicInteger();

    public WebController() {
        final Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        final SenderOptions<String, CreateTransactionCommand> producerOptions = SenderOptions.create(producerProps);
        kafkaProducer = KafkaSender.create(producerOptions);

        final Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "webservice-dispatcher");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        ReceiverOptions<String, String> consumerOptions = ReceiverOptions.create(consumerProps);
        consumerOptions.subscription(Collections.singleton("wallet.transactionCreatedEvent"));
        transactionCreatedEventFlux = KafkaReceiver.create(consumerOptions).receive();
        transactionCreatedEventFlux.log();
        objectMapper = new ObjectMapper();
    }

    @PostMapping(value = "/wallets/transaction")
    public Mono<TransactionCreatedEvent> doTransaction(@RequestBody CreateTransactionCommand command) throws JsonProcessingException {
        LOGGER.info("/transaction received: " + command.toString());

        final String transactionIdFromCommand = command.getOrderId();
        SenderRecord<String, CreateTransactionCommand, Integer> message = SenderRecord.create(new ProducerRecord<>
                        ("wallet.createTransactionCommand",
                        transactionIdFromCommand,
                        command),1);

        kafkaProducer.send(Mono.just(message))
                .doOnError(e -> LOGGER.error("Send failed", e))
                .subscribe(r -> {
            RecordMetadata metadata = r.recordMetadata();
                    LOGGER.info("Message %d sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                    r.correlationMetadata(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    dateFormat.format(new Date(metadata.timestamp())));
        });

        return Mono.just(new TransactionCreatedEvent(command.getOrderId(),
                command.getOrderAmount(),command.getWalletId(), System.currentTimeMillis(), TransactionType.DEPOSIT,command.getDescription()));
    }
}
