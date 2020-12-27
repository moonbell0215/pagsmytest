package com.dht.pags.webservice.dispatcher.controller;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.*;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.util.context.Context;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author cloud.d
 */
@RestController
@RequestMapping("/wallets")
public class WalletController {
    private KafkaSender<String, CreateTransactionCommand> kafkaSender;
    private KafkaReceiver<String, TransactionCreatedEvent> kafkaReceiver;
    private Flux<ReceiverRecord<String, TransactionCreatedEvent>> inboundFlux;
    private Disposable inboundFluxDisposable;
    /**
     * 监听交易处理结果的Topic
     */
    private static final String TOPIC_SEND = "wallet.createTransactionCommand";
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private Sinks.Many<ReceiverRecord<String, TransactionCreatedEvent>> objectMany = Sinks.many().replay().all();

    @PostConstruct
    public void startInboundFlux() {
        LOGGER.info("Called startInboundFlux..");
        inboundFlux = kafkaReceiver.receive();

        inboundFluxDisposable = inboundFlux.subscribe(x ->
        {
            LOGGER.info("inboundFluxS:" + x.value().toString());
            objectMany.tryEmitNext(x);
        });

    }

    @PreDestroy
    public void destory() {
        inboundFluxDisposable.dispose();
    }

    /**
     * 冲正，直接向用户钱包里加钱
     *
     * @return 处理结果
     */
    @PostMapping(path = "/increase", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionCreatedEvent> increase(@RequestBody CreateTransactionCommand command) {
        try {
            LOGGER.info("/transaction received: " + command.toString());

            final String walletId = command.getWalletId();
            final String orderId = command.getOrderId();

            Mono<TransactionCreatedEvent> result = objectMany
                    .asFlux()
                    .share()
                    .filter(r -> r.value().getOrderId().equals(orderId) && r.value().getWalletId().equals(walletId))
                    .map(ReceiverRecord::value)
                    .take(1)  //防止filter後有多於一個結果掛掉
                    .single();
            result.subscribe();

            SenderRecord<String, CreateTransactionCommand, Integer> message = SenderRecord.create(new ProducerRecord<>(TOPIC_SEND, walletId, command), 1);
            kafkaSender.send(Mono.just(message))
                    .doOnError(e -> LOGGER.error("Send failed", e))
                    .doOnNext(r -> LOGGER.info(r.toString()))
                    .subscribe();
            return result;

        } catch (Exception e) {
            LOGGER.error("Error /increase", e.getCause());
            return Mono.error(e);
        }
    }

    @Autowired
    public void setKafkaSender(KafkaSender<String, CreateTransactionCommand> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    @Autowired
    public void setKafkaReceiver(KafkaReceiver<String, TransactionCreatedEvent> kafkaReceiver) {
        this.kafkaReceiver = kafkaReceiver;
    }
}