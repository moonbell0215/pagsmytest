package com.dht.pags.wallet.materalizedviewprocesor;

import com.dht.pags.wallet.materalizedviewprocesor.repository.MaterializedViewUpdateService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class WalletMaterializedViewProcessor {



    @Bean
    public Consumer<MaterializedViewUpdateService> walletTransactionToCosmoDbView() {
        return transactionCreatedEvent -> {
            System.out.println("Received: " + transactionCreatedEvent);
            //TODO : Call CosmoDB Reactive API and store the message into the database
        };
    }

}
