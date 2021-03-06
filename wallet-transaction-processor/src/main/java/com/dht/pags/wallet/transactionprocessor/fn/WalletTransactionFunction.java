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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

@Service
public class WalletTransactionFunction {

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    private ReadOnlyKeyValueStore<String, TransactionCreatedEventSet> keyValueStore;

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

    private Map<String,TransactionCreatedEventSet> tempStore;

    public WalletTransactionFunction() {
        tempStore = new HashMap<>();

    }

    @Bean
    public Function<KStream<String, CreateTransactionCommand>,
            KStream<String, CreateTransactionCommandProcessedEvent>> createTransactionCommandHandler() {
        return (commandKStream) -> {
            commandKStream.peek((key, command) -> LOGGER.info(command.toString()), Named.as("Log-command-received"));

            KStream<String, CreateTransactionCommandProcessedEvent> resultKStream =
                    commandKStream.map((key, command) -> new KeyValue<>(command.getWalletId(), validateAndCreateTransactionCreatedEvent(command))
                            , Named.as("Map-CreateTransactionCommand-to-CreateTransactionCommandProcessedEvent"));

            KStream<String, TransactionCreatedEvent> transactionCreatedEventStream = resultKStream
                    .filter((key, event) -> TransactionStatus.SUCCESS.equals(event.getTransactionStatus()), Named.as("Filter-by-accepted-command"))
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), createTransactionEvent(event)), Named.as("Create-TransactionCreatedEvent-for-accepted-command")
                    );

            //Balance Update ??????????????? TransactionCreatedEvent publish???Kafka???state store ???,?????????????????????
            KStream balanceUpdatedEventStreamAndTransactionCreatedEventStream = transactionCreatedEventStream
                    .flatMap((key, event) -> {
                        List result = new ArrayList<>();
                        result.add(new KeyValue(event.getWalletId(), createBalanceUpdatedEvent(event)));
                        result.add(new KeyValue(event.getWalletId(), event));
                        result.add(new KeyValue(event.getWalletId(), updateTransactionCreatedEventList(event)));
                        return result;
                    }, Named.as("FlatMap-TransactionCreatedEvent-to-BalanceUpdatedEvent-And-updateTransactionCreatedEventList"));

            balanceUpdatedEventStreamAndTransactionCreatedEventStream.filter((key, value) -> value instanceof BalanceUpdatedEvent, Named.as("Filter-And-Store-BalanceUpdatedEvent")).to(BALANCE_UPDATED_TOPIC_NAME, Produced.with(Serdes.String(), new BalanceUpdatedEventSerde()));
            balanceUpdatedEventStreamAndTransactionCreatedEventStream.filter((key, value) -> value instanceof TransactionCreatedEvent, Named.as("Filter-And-Store-TransactionCreatedEvent")).to(SUCCESS_TRANSACTION_TOPIC_NAME, Produced.with(Serdes.String(), new TransactionCreatedEventSerde()));
            balanceUpdatedEventStreamAndTransactionCreatedEventStream.filter((key, value) -> value instanceof TransactionCreatedEventSet, Named.as("Filter-And-Store-" + STORE_NAME)).toTable(Named.as("STATE-STORE"), Materialized.<String, TransactionCreatedEventSet, KeyValueStore<Bytes, byte[]>>as(STORE_NAME).withKeySerde(Serdes.String()).withValueSerde(new TransactionCreatedEventSetSerde()));

            return resultKStream;
        };
    }

    private TransactionCreatedEventSet getTransactionCreatedEventSetFromStateStore(String walletId) {
        if (keyValueStore == null) {
            keyValueStore = interactiveQueryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.keyValueStore());
        }
        TransactionCreatedEventSet tempStoreEventSet = tempStore.get(walletId);
        if(tempStoreEventSet != null)
        {
            return tempStoreEventSet;
        }

        TransactionCreatedEventSet stateStoreEventSet = keyValueStore.get(walletId);
        if(stateStoreEventSet != null)
        {
            tempStore.put(walletId,stateStoreEventSet);
        }

        return stateStoreEventSet;
    }

    private TransactionCreatedEventSet updateTransactionCreatedEventList(TransactionCreatedEvent event) {
        TransactionCreatedEventSet eventSet = Optional.ofNullable(getTransactionCreatedEventSetFromStateStore(event.getWalletId())).orElse(new TransactionCreatedEventSet());
        LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
        boolean added = eventSet.getEventSet().add(event);
        this.tempStore.put(event.getWalletId(),eventSet);
        LOGGER.info("TransactionCreatedEvent added to eventSet:" + added);
        if (!added) {
            throw new RuntimeException("TransactionCreatedEvent is not added to eventSet!" + event.toString());
        }
        return eventSet;
    }

    private CreateTransactionCommandProcessedEvent validateAndCreateTransactionCreatedEvent(CreateTransactionCommand createTransactionCommand) {
        return createTransactionCommandProcessedEvent(createTransactionCommand, this.validateCreateTransactionCommand(createTransactionCommand) ? TransactionStatus.SUCCESS : TransactionStatus.FAILURE);
    }

    private boolean validateCreateTransactionCommand(CreateTransactionCommand createTransactionCommand) {
        if(createTransactionCommand.getOperatorType() == null || "null".equals(createTransactionCommand.getOperatorType())){
            LOGGER.info("OperatorType is null");
            return false;
        }
        if (!createTransactionCommand.getTransactionType().isReduce()) {
            return true;
        }
        TransactionCreatedEventSet eventSet = getTransactionCreatedEventSetFromStateStore(createTransactionCommand.getWalletId());
        return eventSet != null &&
                eventSet.getEventSet().stream().map(TransactionCreatedEvent::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(createTransactionCommand.getOrderAmount().abs()) >= 0;
    }

    private TransactionCreatedEvent createTransactionEvent(CreateTransactionCommandProcessedEvent processedEvent) {
        //TODO: Implement logic
        return new TransactionCreatedEvent(processedEvent.getId(),
                processedEvent.getOrderId(),
                processedEvent.getTransactionAmount(),
                processedEvent.getWalletId(),
                processedEvent.getTransactionDateTime(),
                processedEvent.getTransactionType(),
                processedEvent.getOperatorType(),
                processedEvent.getDescription()
        );
    }

    private BalanceUpdatedEvent createBalanceUpdatedEvent(TransactionCreatedEvent event) {
        TransactionCreatedEventSet eventSet = getTransactionCreatedEventSetFromStateStore(event.getWalletId());
        BigDecimal previousBalance = new BigDecimal("0");
        BigDecimal newBalance;

        if (eventSet != null) {
            //LOGGER.info("Event Store size is " + eventSet.getpreviousBalanceEventSet().size());
            previousBalance = eventSet.getEventSet().stream().map(TransactionCreatedEvent::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            newBalance = previousBalance.add(event.getTransactionAmount());

        } else {
            LOGGER.info("Event Store is empty, key=" + event.getWalletId());
            newBalance = event.getTransactionAmount();
        }
        LOGGER.info("id = " + event.getId() + ",  Wallet:" + event.getWalletId() + " ,previousBalance:" + previousBalance + ", transactionAmount = " + event.getTransactionAmount() + ", newBalance:" + newBalance);
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
            //LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
            id += "-" + (eventSet.getEventSet().size() + 1);
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
                createTransactionCommand.getOperatorType(),
                "Note by event:" + createTransactionCommand.getDescription(),
                transactionStatus);
    }

}
