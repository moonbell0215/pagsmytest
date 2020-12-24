package com.dht.pags.webservice.dispatcher.controller;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import com.dht.pags.wallet.domain.TransactionType;
import com.dht.pags.webservice.dispatcher.model.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

    private static final String TOPIC_SEND = "wallet.createTransactionCommand";

    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS z dd MMM yyyy");

    /**
     * 获取指定用户钱包余额
     * 这个功能似乎是在inquiry-processor中处理的，这里暂时略过
     *
     * @param employeecode
     * @return
     */
    @Deprecated
    @GetMapping(path = "/Balance/{employeecode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Wallet> getBalance(@PathVariable("employeecode") String employeecode) {
        if ("E0000001".equals(employeecode)) {
            Wallet wallet = new Wallet();
            wallet.setEmployeecode("E0000001");
            wallet.setCurrency("CNY");
            wallet.setBalance(new BigDecimal("0.00"));
            wallet.setUpdatetime(System.currentTimeMillis());
            return Mono.just(wallet);
        } else {
            return Mono.error(new Exception("User Not Found!"));
        }
    }

    /**
     * 冲正，直接向用户钱包里加钱
     *
     * @return
     */
    @PostMapping(path = "/Increase", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionCreatedEvent> increase(@RequestBody CreateTransactionCommand command) {
        try {
            LOGGER.info("/transaction received: " + command.toString());
            final String transactionIdFromCommand = command.getTransactionId();
            SenderRecord<String, CreateTransactionCommand, Integer> message = SenderRecord.create(new ProducerRecord<>(TOPIC_SEND, transactionIdFromCommand, command), 1);
            kafkaSender.send(Mono.just(message))
                    .doOnError(e -> LOGGER.error("Send failed", e))
                    .subscribe();

            Flux<ReceiverRecord<String, TransactionCreatedEvent>> kafkaFlux = kafkaReceiver.receive();
            kafkaFlux.filter(x -> x.value().getId().equals(transactionIdFromCommand))
                    .log()
                    .doOnNext(r -> r.receiverOffset().acknowledge())
                    .map(ReceiverRecord::value)
                    .doOnNext(r -> hander(r))
                    .doOnError(e -> LOGGER.error("Receive failed", e))
                    .subscribe();

            kafkaFlux.log()
                    .doOnNext(r -> r.receiverOffset().acknowledge())
                    .map(ReceiverRecord::value)
                    .doOnNext(r -> LOGGER.info(r.toString()))
                    .doOnError(e -> LOGGER.error("Receiver failed", e))
                    .subscribe();

            // 这里的sample https://projectreactor.io/docs/kafka/release/reference/#api-guide-receiver
            kafkaFlux.subscribe(r -> {
               LOGGER.info("receive message from kafka: %s\n" + r);
               r.receiverOffset().acknowledge();
            });



            return Mono.just(new TransactionCreatedEvent(command.getTransactionId(), command.getTransactionAmount(),
                    command.getWalletId(), System.currentTimeMillis(), TransactionType.DEPOSIT, command.getDescription()));
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.error(e);
        }
    }

    public void hander(Object obj) {
        System.out.println(obj);
    }
}