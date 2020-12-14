package com.dht.pags.wallet.transactionprocessor.fn;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class WalletTransactionFunction {

    @Bean
    public Function<Message, Message> uppercase() {
        return value -> {
            System.out.println("Received: " + value.getPayload());
            return MessageBuilder.withPayload(value.getPayload().toString().toUpperCase()).build();
        };
    }

}
