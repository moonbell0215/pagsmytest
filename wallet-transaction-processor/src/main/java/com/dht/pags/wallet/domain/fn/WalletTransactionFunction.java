package com.dht.pags.wallet.domain.fn;

import com.dht.pags.wallet.domain.CreateTransactionCommand;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class WalletTransactionFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletTransactionFunction.class);
    @Bean
    public BiFunction<KStream<String, CreateTransactionCommand>, KStream<String, TransactionCreatedEvent>, KStream<String, TransactionCreatedEvent>> createTransactionCommandHandler() {
        return (createTransactionCommand,transactionCreatedEventKStream) -> {
            transactionCreatedEventKStream.peek((k, v) -> LOGGER.info("transactionCreatedEventKStream: " + v));
            return createTransactionCommand.peek((k, v) -> LOGGER.info("CreateTransactionCommand: " + v))
                    .map((key, create) -> new KeyValue<>(key, validateAndCreateTransactionCreatedEvent(create)));
        };
    }
//    @Bean
//    public Function<KStream<String, CreateTransactionCommand>, KStream<String, TransactionCreatedEvent>> createTransactionCommandHandler() {
//        return input -> input
//                .map((key, createTransactionCommand) -> new KeyValue(key, validateAndCreateTransactionCreatedEvent(createTransactionCommand)));
//    }

    private TransactionCreatedEvent validateAndCreateTransactionCreatedEvent(CreateTransactionCommand createTransactionCommand) {
        if (this.validateCreateTransactionCommand(createTransactionCommand)) {
            return createTransactionEvent(createTransactionCommand);
        }
        //TODO : Return error
        return null;
    }

    private boolean validateCreateTransactionCommand(CreateTransactionCommand createTransactionCommand) {
        //TODO: Implement validation logic
        return true;
    }

    private TransactionCreatedEvent createTransactionEvent(CreateTransactionCommand createTransactionCommand) {
        //TODO: Implement logic
        return new TransactionCreatedEvent(createTransactionCommand.getTransactionId(),
                createTransactionCommand.getTransactionAmount(),
                createTransactionCommand.getWalletId(),
                createTransactionCommand.getTransactionDateTime(),
                createTransactionCommand.getTransactionType(),
                "Note by event:" + createTransactionCommand.getDescription()
        );
    }

}
