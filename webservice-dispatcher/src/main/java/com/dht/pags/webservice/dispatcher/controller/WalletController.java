package com.dht.pags.webservice.dispatcher.controller;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import com.dht.pags.webservice.dispatcher.model.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.math.BigDecimal;

/**
 * @author cloud.d
 */
@RestController
@RequestMapping("/Wallet")
public class WalletController {
    @Autowired
    private KafkaSender<String, String> kafkaSender;
    @Autowired
    private KafkaReceiver<Object, TransactionCreatedEvent> kafkaReceiver;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOPIC_SEND = "wallet.createTransactionCommand";
    /**
     * 获取指定用户钱包余额
     * 这个功能似乎是在inquiry-processor中处理的，这里暂时略过
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
     * @return
     */
    @PostMapping(path = "/Increase", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ReceiverRecord<Object, TransactionCreatedEvent>> increase(@RequestBody CreateTransactionCommand command) {
        try {
            System.out.println("Request is coming \n" + command);
            final String transactionIdFromCommand = command.getTransactionId();
            SenderRecord<String, String, Integer> message = SenderRecord.create(new ProducerRecord<>(TOPIC_SEND, objectMapper.writeValueAsString(command)), 1);
            kafkaSender.send(Mono.just(message));
            Flux<ReceiverRecord<Object, TransactionCreatedEvent>> transactionCreatedEventFlux = kafkaReceiver.receive();
            // TODO 下面这一步会报错
            return transactionCreatedEventFlux.filter(x -> x.value().getId().equals(transactionIdFromCommand)).next();
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.error(e);
        }
    }
}