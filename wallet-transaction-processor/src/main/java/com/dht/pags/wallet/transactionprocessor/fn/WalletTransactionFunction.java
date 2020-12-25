package com.dht.pags.wallet.transactionprocessor.fn;

import com.dht.pags.wallet.domain.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class WalletTransactionFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletTransactionFunction.class);
    public static final class TransactionCreatedEventSerde extends JsonSerde<TransactionCreatedEvent> { }
    public static final class BalanceUpdatedEventSerde extends JsonSerde<BalanceUpdatedEvent> { }

    @Value("${application.topic.name.out-1}")
    private String SUCCESS_TRANSACTION_TOPIC_NAME; //wallet.transactionCreatedEvent
    @Value("${application.topic.name.out-2}")
    private String BALANCE_UPDATED_TOPIC_NAME; //wallet.balanceUpdatedEvent

    @Bean
    public Function<KStream<String, CreateTransactionCommand>,
                KStream<String, CreateTransactionCommandProcessedEvent>> createTransactionCommandHandler() {
        return (commandKStream) -> {
            commandKStream.peek((key, value) -> LOGGER.info(value.toString()));

            KStream<String, CreateTransactionCommandProcessedEvent> resultKStream =
                    commandKStream.map((key, command) -> new KeyValue<>(key, validateAndCreateTransactionCreatedEvent(command))
                    );

            KStream<String, TransactionCreatedEvent> transactionCreatedEventStream = resultKStream
                    .filter((key, value) -> TransactionStatus.SUCCESS.equals(value.getTransactionStatus()))
                    .map((key, event) -> new KeyValue<>(key, createTransactionEvent(event.getCommand()))
                    );
            transactionCreatedEventStream.to(SUCCESS_TRANSACTION_TOPIC_NAME,Produced.with(Serdes.String(),new TransactionCreatedEventSerde()));

            transactionCreatedEventStream
                    .map((key, event) -> new KeyValue<>(event.getId(), event))
                    .toTable(Materialized.<String,TransactionCreatedEvent,KeyValueStore<Bytes,byte[]>> as("TransactionCreatedEvent").withKeySerde(Serdes.String()).withValueSerde(new TransactionCreatedEventSerde()));

            KStream<String, BalanceUpdatedEvent> balanceUpdatedEventStream = resultKStream
                    .filter((key, value) -> TransactionStatus.SUCCESS.equals(value.getTransactionStatus()))
                    .map((key, event) -> new KeyValue<>(key, createBalanceUpdatedEvent(event.getCommand()))
                    );
            balanceUpdatedEventStream.to(BALANCE_UPDATED_TOPIC_NAME,Produced.with(Serdes.String(),new BalanceUpdatedEventSerde()));

            return resultKStream;
        };
    }

    private CreateTransactionCommandProcessedEvent validateAndCreateTransactionCreatedEvent(CreateTransactionCommand createTransactionCommand) {
        if (this.validateCreateTransactionCommand(createTransactionCommand)) {
            return createTransactionCommandProcessedEvent(createTransactionCommand, TransactionStatus.SUCCESS);
        }
        //TODO : Return error
        return createTransactionCommandProcessedEvent(createTransactionCommand, TransactionStatus.FAILURE);
    }

    private boolean validateCreateTransactionCommand(CreateTransactionCommand createTransactionCommand) {
        //TODO: Implement validation logic
        //SUCCESS orderAmount 大於 100
        if (createTransactionCommand.getOrderAmount() > 100) {
            return true;
        } else {
            //test FAILURE orderAmount 小於等於 100
            return false;
        }

    }

    private TransactionCreatedEvent createTransactionEvent(CreateTransactionCommand command) {
        //TODO: Implement logic
        return new TransactionCreatedEvent(UUID.randomUUID().toString(),
                command.getOrderId(),
                command.getOrderAmount(),
                command.getWalletId(),
                new Date().getTime(),
                command.getTransactionType(),
                command.getDescription()
        );
    }

    private BalanceUpdatedEvent createBalanceUpdatedEvent(CreateTransactionCommand command) {
        //TODO: Implement logic
        return new BalanceUpdatedEvent(command.getOrderId(),
                command.getOrderAmount(),
                command.getWalletId(),
                command.getOrderAmount() * 2
        );
    }

    private CreateTransactionCommandProcessedEvent createTransactionCommandProcessedEvent(CreateTransactionCommand createTransactionCommand, TransactionStatus transactionStatus) {
        //TODO: Implement logic
        return new CreateTransactionCommandProcessedEvent(
                //TODO-暫時使用createTransactionCommand.getWalletId() + createTransactionCommand.getOrderId()
                //state store . Key = wallet Id . Value list <TransactionCreatedEvent>
                //數一下list size 就知道下一個sequence
                //開發中
                createTransactionCommand.getWalletId() + "-" + createTransactionCommand.getOrderId(),
                createTransactionCommand.getOrderAmount(),
                createTransactionCommand.getOrderId(),
                createTransactionCommand.getWalletId(),
                System.currentTimeMillis(),
                createTransactionCommand.getTransactionType(),
                "Note by event:" + createTransactionCommand.getDescription(),
                transactionStatus, createTransactionCommand);
    }
}
