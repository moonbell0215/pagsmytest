package com.dht.pags.wallet.transactionprocessor.fn;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

@Service
public class WalletTransactionFunction {

    @Bean
    public Function<String, String> placeTradeCommandHandler() {
        return value -> {
            System.out.println("Order      Received: " + value);
            return value.toUpperCase();
        };
    }




}
