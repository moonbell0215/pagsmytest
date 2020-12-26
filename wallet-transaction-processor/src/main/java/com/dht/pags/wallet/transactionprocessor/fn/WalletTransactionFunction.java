package com.dht.pags.wallet.transactionprocessor.fn;

import com.dht.pags.wallet.domain.*;
import com.dht.pags.wallet.transactionprocessor.domain.TransactionCreatedEventSet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
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

import java.util.Optional;
import java.util.UUID;
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
                    commandKStream.map((key, command) -> new KeyValue<>(key, validateAndCreateTransactionCreatedEvent(command))
                    );

            KStream<String, TransactionCreatedEvent> transactionCreatedEventStream = resultKStream
                    .filter((key, event) -> TransactionStatus.SUCCESS.equals(event.getTransactionStatus()))
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), createTransactionEvent(event, event.getCommand()))
                    );
            transactionCreatedEventStream.to(SUCCESS_TRANSACTION_TOPIC_NAME, Produced.with(Serdes.String(), new TransactionCreatedEventSerde()));

            transactionCreatedEventStream
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), updateTransactionCreatedEventList(event)))
                    .toTable(Materialized.<String, TransactionCreatedEventSet, KeyValueStore<Bytes, byte[]>>as(STORE_NAME).withKeySerde(Serdes.String()).withValueSerde(new TransactionCreatedEventSetSerde()));

            KStream<String, BalanceUpdatedEvent> balanceUpdatedEventStream = resultKStream
                    .filter((key, event) -> TransactionStatus.SUCCESS.equals(event.getTransactionStatus()))
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), createBalanceUpdatedEvent(event.getId(), event.getCommand()))
                    );
            balanceUpdatedEventStream.to(BALANCE_UPDATED_TOPIC_NAME, Produced.with(Serdes.String(), new BalanceUpdatedEventSerde()));

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
                eventSet.getEventSet().stream().mapToDouble(TransactionCreatedEvent::getTransactionAmount).sum() >= createTransactionCommand.getOrderAmount();
    }

    private TransactionCreatedEvent createTransactionEvent(CreateTransactionCommandProcessedEvent processedEvent, CreateTransactionCommand command) {
        //TODO: Implement logic
        return new TransactionCreatedEvent(processedEvent.getId(),
                command.getOrderId(),
                command.getOrderAmount(),
                command.getWalletId(),
                processedEvent.getTransactionDateTime(),
                command.getTransactionType(),
                command.getDescription()
        );
    }

    private BalanceUpdatedEvent createBalanceUpdatedEvent(String id, CreateTransactionCommand command) {
        TransactionCreatedEventSet eventSet = getTransactionCreatedEventSetFromStateStore(command.getWalletId());
        double beforeBalance = 0;
        double balance = 0;

        if (eventSet != null) {
            LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
            beforeBalance = eventSet.getEventSet().stream().mapToDouble(TransactionCreatedEvent::getTransactionAmount).sum();
            balance = beforeBalance + command.getOrderAmount();

        } else {
            LOGGER.info("Event Store is empty, key=" + command.getWalletId());
            balance = command.getOrderAmount();
        }
        LOGGER.info(" key= "+command.getWalletId()+" ,beforeBalance= " + beforeBalance +" , balance= "+balance);
        //TODO: Implement logic
        return new BalanceUpdatedEvent(id,
                command.getOrderAmount(),
                command.getWalletId(),
                balance,
                beforeBalance);
    }

    private CreateTransactionCommandProcessedEvent createTransactionCommandProcessedEvent(CreateTransactionCommand createTransactionCommand, TransactionStatus transactionStatus) {
        //TODO: Implement logic
        return new CreateTransactionCommandProcessedEvent(
                //TODO-暫時使用UUID.randomUUID().toString()
                UUID.randomUUID().toString(),
                createTransactionCommand.getOrderAmount(),
                createTransactionCommand.getOrderId(),
                createTransactionCommand.getWalletId(),
                System.currentTimeMillis(),
                createTransactionCommand.getTransactionType(),
                "Note by event:" + createTransactionCommand.getDescription(),
                transactionStatus, createTransactionCommand);
    }
}
