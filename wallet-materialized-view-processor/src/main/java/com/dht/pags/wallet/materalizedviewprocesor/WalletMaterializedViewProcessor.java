package com.dht.pags.wallet.materalizedviewprocesor;

import com.azure.data.cosmos.CosmosItemResponse;
import com.dht.pags.wallet.materalizedviewprocesor.repository.MaterializedViewUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Service
public class WalletMaterializedViewProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletMaterializedViewProcessor.class);

    @Autowired
    private MaterializedViewUpdateService materializedViewUpdateService;

    @Bean
    public Consumer<MaterializedViewUpdateService> walletTransactionToCosmoDbView() {
        return transactionCreatedEvent -> {
            LOGGER.info("Received: " + transactionCreatedEvent);
            Mono<CosmosItemResponse> cosmosItemResponseMono = materializedViewUpdateService.createItem(transactionCreatedEvent);
            cosmosItemResponseMono.log();
        };
    }

}
