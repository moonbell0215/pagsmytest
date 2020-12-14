package com.dht.pags.wallet.transactionprocessor.controller;

import com.dht.pags.wallet.transactionprocessor.domain.WalletMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SendMessageController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaTemplate<String, WalletMessage> kafkaTemplate;

    @GetMapping("send/{userId}/{message}")
    public void sendMessage(@PathVariable String userId,@PathVariable String message) {
        this.kafkaTemplate.send("test", new WalletMessage(userId, message));
    }
}
