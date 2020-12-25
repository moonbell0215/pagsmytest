package com.dht.pags.webservice.dispatcher.controller;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * @author cloud.d
 */
@RestController
@RequestMapping("/Wallet")
public class WalletController {
    private static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private KafkaSender<String, CreateTransactionCommand> kafkaSender;
    @Autowired
    private KafkaReceiver<String, TransactionCreatedEvent> kafkaReceiver;
    /**
     *
     */
    private static final String TOPIC_SEND = "wallet.createTransactionCommand";

    /**
     * 冲正，直接向用户钱包里加钱
     *
     * @return
     */
    @PostMapping(path = "/Increase", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionCreatedEvent> increase(@RequestBody CreateTransactionCommand command) {
        try {
            LOGGER.info("/transaction received: " + command.toString());
            final String transactionIdFromCommand = command.getOrderId();
            SenderRecord<String, CreateTransactionCommand, Integer> message = SenderRecord.create(new ProducerRecord<>(TOPIC_SEND, transactionIdFromCommand, command), 1);
            kafkaSender.send(Mono.just(message))
                    .doOnError(e -> LOGGER.error("Send failed", e))
                    .subscribe();

            return kafkaReceiver.receive()
                    .checkpoint("========== Messages are started being consumed. ==========")
                    .log()
                    .filter(x -> x.value().getId().equals(transactionIdFromCommand))
                    .doOnError(e -> LOGGER.error("Receive failed", e))
                    .doOnNext((r -> r.receiverOffset().acknowledge()))
                    .map(ReceiverRecord::value)
                    .next()
                    .checkpoint("========== Messages are done consumed. ==========");
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.error(e);
        }
    }
}