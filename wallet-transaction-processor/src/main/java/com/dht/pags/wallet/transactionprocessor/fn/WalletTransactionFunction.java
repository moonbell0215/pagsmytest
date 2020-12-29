package com.dht.pags.wallet.transactionprocessor.fn;

import com.dht.pags.wallet.domain.*;
import com.dht.pags.wallet.transactionprocessor.domain.TransactionCreatedEventSet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class WalletTransactionFunction {

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletTransactionFunction.class);

    public static final class TransactionCreatedEventSerde extends JsonSerde<TransactionCreatedEvent> {
    }

    public static final class TransactionCreatedEventSetSerde extends JsonSerde<TransactionCreatedEventSet> {
    }

    public static final class BalanceUpdatedEventSerde extends JsonSerde<BalanceUpdatedEvent> {
    }

    @Value("${application.topic.name.out-1}")
    private String SUCCESS_TRANSACTION_TOPIC_NAME; //wallet.transactionCreatedEvent
    @Value("${application.topic.name.out-2}")
    private String BALANCE_UPDATED_TOPIC_NAME; //wallet.balanceUpdatedEvent
    @Value("${application.topic.name.store}")
    private String STORE_NAME;

    public WalletTransactionFunction() {

    }

    @Bean
    public Function<KStream<String, CreateTransactionCommand>,
            KStream<String, CreateTransactionCommandProcessedEvent>> createTransactionCommandHandler() {
        return (commandKStream) -> {
            commandKStream.peek((key, command) -> LOGGER.info(command.toString()));

            KStream<String, CreateTransactionCommandProcessedEvent> resultKStream =
                    commandKStream.map((key, command) -> new KeyValue<>(command.getWalletId(), validateAndCreateTransactionCreatedEvent(command))
                    );

            KStream<String, TransactionCreatedEvent> transactionCreatedEventStream = resultKStream
                    .filter((key, event) -> TransactionStatus.SUCCESS.equals(event.getTransactionStatus()))
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), createTransactionEvent(event))
                    );

            //Balance Update 必須發生在 TransactionCreatedEvent publish到Kafka及state store 前,否則有不確定性
            KStream balanceUpdatedEventStreamAndTransactionCreatedEventStream = transactionCreatedEventStream
                    .flatMap((key, event) -> {
                        List result = new ArrayList<>();
                        result.add(new KeyValue(event.getWalletId(), createBalanceUpdatedEvent(event)));
                        result.add(new KeyValue(event.getWalletId(),event));
                        result.add(new KeyValue(event.getWalletId(),updateTransactionCreatedEventList(event)));
                        return result;
                    });

            balanceUpdatedEventStreamAndTransactionCreatedEventStream.filter((key, value) -> value instanceof BalanceUpdatedEvent).to(BALANCE_UPDATED_TOPIC_NAME, Produced.with(Serdes.String(), new BalanceUpdatedEventSerde()));
            balanceUpdatedEventStreamAndTransactionCreatedEventStream.filter((key, value) -> value instanceof TransactionCreatedEvent).to(SUCCESS_TRANSACTION_TOPIC_NAME, Produced.with(Serdes.String(), new TransactionCreatedEventSerde()));
            balanceUpdatedEventStreamAndTransactionCreatedEventStream.filter((key, value) -> value instanceof TransactionCreatedEventSet).toTable(Materialized.<String, TransactionCreatedEventSet, KeyValueStore<Bytes, byte[]>>as(STORE_NAME).withKeySerde(Serdes.String()).withValueSerde(new TransactionCreatedEventSetSerde()));

            return resultKStream;
        };
    }

    private TransactionCreatedEventSet getTransactionCreatedEventSetFromStateStore(String walletId) {
        ReadOnlyKeyValueStore<String, TransactionCreatedEventSet> keyValueStore = interactiveQueryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.keyValueStore());
        return keyValueStore.get(walletId);
    }

    private TransactionCreatedEventSet updateTransactionCreatedEventList(TransactionCreatedEvent event) {
        TransactionCreatedEventSet eventSet = Optional.ofNullable(getTransactionCreatedEventSetFromStateStore(event.getWalletId())).orElse(new TransactionCreatedEventSet());
        LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
        eventSet.getEventSet().forEach(x -> LOGGER.info(x.toString()));
        eventSet.getEventSet().add(event);
        return eventSet;
    }

    private CreateTransactionCommandProcessedEvent validateAndCreateTransactionCreatedEvent(CreateTransactionCommand createTransactionCommand) {
        return createTransactionCommandProcessedEvent(createTransactionCommand, this.validateCreateTransactionCommand(createTransactionCommand) ? TransactionStatus.SUCCESS : TransactionStatus.FAILURE);
    }

    private boolean validateCreateTransactionCommand(CreateTransactionCommand createTransactionCommand) {
        if (!createTransactionCommand.getTransactionType().isReduce()) {
            return true;
        }
        TransactionCreatedEventSet eventSet = getTransactionCreatedEventSetFromStateStore(createTransactionCommand.getWalletId());
        return eventSet != null &&
                eventSet.getEventSet().stream().mapToDouble(TransactionCreatedEvent::getTransactionAmount).sum() >= Math.abs(createTransactionCommand.getOrderAmount());
    }

    private TransactionCreatedEvent createTransactionEvent(CreateTransactionCommandProcessedEvent processedEvent) {
        //TODO: Implement logic
        return new TransactionCreatedEvent(processedEvent.getId(),
                processedEvent.getOrderId(),
                processedEvent.getTransactionAmount(),
                processedEvent.getWalletId(),
                processedEvent.getTransactionDateTime(),
                processedEvent.getTransactionType(),
                processedEvent.getDescription()
        );
    }

    private BalanceUpdatedEvent createBalanceUpdatedEvent(TransactionCreatedEvent event) {
        TransactionCreatedEventSet eventSet = getTransactionCreatedEventSetFromStateStore(event.getWalletId());
        double previousBalance = 0;
        double newBalance;

        if (eventSet != null) {
            LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
            previousBalance = eventSet.getEventSet().stream().mapToDouble(TransactionCreatedEvent::getTransactionAmount).sum();
            newBalance = previousBalance + event.getTransactionAmount();

        } else {
            LOGGER.info("Event Store is empty, key=" + event.getWalletId());
            newBalance = event.getTransactionAmount();
        }
        LOGGER.info("id = " + event.getId() + ",  Wallet:"+event.getWalletId()+" ,previousBalance:" + previousBalance + ", transactionAmount = " + event.getTransactionAmount() + ", newBalance:"+newBalance);
        //TODO: Implement logic
        return new BalanceUpdatedEvent(event.getId(),
                event.getTransactionAmount(),
                event.getWalletId(),
                event.getTransactionDateTime(),
                newBalance,
                previousBalance);
    }

    private CreateTransactionCommandProcessedEvent createTransactionCommandProcessedEvent(CreateTransactionCommand createTransactionCommand, TransactionStatus transactionStatus) {
        //TODO: Implement logic
        TransactionCreatedEventSet eventSet = getTransactionCreatedEventSetFromStateStore(createTransactionCommand.getWalletId());
        String id = createTransactionCommand.getWalletId();
        if (eventSet != null) {
            LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
            id +=  "-" +(eventSet.getEventSet().size() +1);
        } else {
            id += "-1";
        }

        return new CreateTransactionCommandProcessedEvent(
                id,
                createTransactionCommand.getOrderAmount(),
                createTransactionCommand.getOrderId(),
                createTransactionCommand.getWalletId(),
                System.currentTimeMillis(),
                createTransactionCommand.getTransactionType(),
                "Note by event:" + createTransactionCommand.getDescription(),
                transactionStatus);
    }
}
