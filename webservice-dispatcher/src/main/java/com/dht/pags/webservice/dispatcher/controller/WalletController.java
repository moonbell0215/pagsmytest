package com.dht.pags.webservice.dispatcher.controller;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.CreateTransactionCommandProcessedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author cloud.d
 */
@RestController
@RequestMapping("/wallets")
public class WalletController {
    private KafkaSender<String, CreateTransactionCommand> kafkaSender;
    private KafkaReceiver<String, CreateTransactionCommandProcessedEvent> kafkaReceiver;
    private Flux<ReceiverRecord<String, CreateTransactionCommandProcessedEvent>> inboundFlux;
    private Disposable inboundFluxDisposable;
    /**
     * 监听交易处理结果的Topic
     */
    private static final String TOPIC_SEND = "wallet.createTransactionCommand";
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private Sinks.Many<ReceiverRecord<String, CreateTransactionCommandProcessedEvent>> objectMany = Sinks.many().replay().all();

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
    @PostMapping(path = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CreateTransactionCommandProcessedEvent> increase(@RequestBody CreateTransactionCommand command) {
        try {
            LOGGER.debug("/transaction received: " + command.toString());

            final String walletId = command.getWalletId();
            final String orderId = command.getOrderId();

            Mono<CreateTransactionCommandProcessedEvent> result = objectMany
                    .asFlux()
                    .share() //Mirror to 新的Flux, 這就不會和其他同時進行的 restful request filter override.
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
    public void setKafkaReceiver(KafkaReceiver<String, CreateTransactionCommandProcessedEvent> kafkaReceiver) {
        this.kafkaReceiver = kafkaReceiver;
    }
}