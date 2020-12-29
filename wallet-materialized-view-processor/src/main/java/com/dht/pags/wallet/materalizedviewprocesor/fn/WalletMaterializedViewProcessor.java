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
            TransactionCreatedEvent transactionCreatedEvent = new TransactionCreatedEvent(event.getWalletId()+"-"+event.getTransactionDateTime(),
                    event.getOrderId(), event.getTransactionAmount(), event.getWalletId(), event.getTransactionDateTime(), event.getTransactionType(), event.getDescription());
            LOGGER.info("ReceivedTran: " + transactionCreatedEvent.getId());
            materializedViewUpdateService.createItem(transactionCreatedEvent).log().subscribe();
        };
    }

    @Bean
    public Consumer<BalanceUpdatedEvent> balanceUpdatedEventToCosmoDbView() {
        return event -> {
            LOGGER.info("ReceivedBalance: " + event);
            BalanceUpdatedEvent balanceUpdatedEvent = new BalanceUpdatedEvent(event.getWalletId()+"-"+event.getUpdateTime(),
                    event.getTransactionAmount(), event.getWalletId(), event.getUpdateTime(), event.getBalance(), event.getBeforeBalance());
            LOGGER.info("ReceivedBalance: " + balanceUpdatedEvent.getId());
            materializedViewUpdateService.createBalance(balanceUpdatedEvent).log().subscribe();
        };
    }

}
