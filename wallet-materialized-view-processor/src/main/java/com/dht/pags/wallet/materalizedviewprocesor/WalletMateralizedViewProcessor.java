package com.dht.pags.wallet.materalizedviewprocesor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class WalletMateralizedViewProcessor {

    @Bean
    public Consumer<String> walletTransactionToCosmoDbView() {
        return value -> {
            System.out.println("Received: " + value);
            //TODO : Call CosmoDB Reactive API and store the message into the database
        };
    }

}
