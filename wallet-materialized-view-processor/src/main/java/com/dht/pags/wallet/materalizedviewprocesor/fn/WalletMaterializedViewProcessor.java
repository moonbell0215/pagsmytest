package com.dht.pags.wallet.materalizedviewprocesor.fn;

import com.dht.pags.wallet.domain.BalanceUpdatedEvent;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import com.dht.pags.wallet.materalizedviewprocesor.repository.MaterializedViewUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class WalletMaterializedViewProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletMaterializedViewProcessor.class);

    @Autowired
    private MaterializedViewUpdateService materializedViewUpdateService;

    @Bean
    public Consumer<TransactionCreatedEvent> walletTransactionToCosmoDbView() {
        return event -> {
            LOGGER.info("ReceivedTran: " + event);
            materializedViewUpdateService.createItem(event).log().subscribe();
        };
    }

    @Bean
    public Consumer<BalanceUpdatedEvent> balanceUpdatedEventToCosmoDbView() {
        return event -> {
            LOGGER.info("ReceivedBalance: " + event);
            materializedViewUpdateService.createBalance(event).log().subscribe();
        };
    }

}
