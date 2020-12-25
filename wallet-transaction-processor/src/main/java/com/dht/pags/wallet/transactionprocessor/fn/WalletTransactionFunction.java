package com.dht.pags.wallet.transactionprocessor.fn;

import com.dht.pags.wallet.domain.*;
import com.dht.pags.wallet.transactionprocessor.domain.TransactionCreatedEventSet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
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

    public static final String eventStoreName = "TransactionCreatedEventStore";

    @Value("${application.topic.name.out-1}")
    private String SUCCESS_TRANSACTION_TOPIC_NAME; //wallet.transactionCreatedEvent
    @Value("${application.topic.name.out-2}")
    private String BALANCE_UPDATED_TOPIC_NAME; //wallet.balanceUpdatedEvent

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
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), createTransactionEvent(event.getCommand()))
                    );
            transactionCreatedEventStream.to(SUCCESS_TRANSACTION_TOPIC_NAME, Produced.with(Serdes.String(), new TransactionCreatedEventSerde()));

            transactionCreatedEventStream
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), updateTransactionCreatedEventList(event)))
                    .toTable(Materialized.<String, TransactionCreatedEventSet, KeyValueStore<Bytes, byte[]>>as(eventStoreName).withKeySerde(Serdes.String()).withValueSerde(new TransactionCreatedEventSetSerde()));

            KStream<String, BalanceUpdatedEvent> balanceUpdatedEventStream = resultKStream
                    .filter((key, event) -> TransactionStatus.SUCCESS.equals(event.getTransactionStatus()))
                    .map((key, event) -> new KeyValue<>(event.getWalletId(), createBalanceUpdatedEvent(event.getCommand()))
                    );
            balanceUpdatedEventStream.to(BALANCE_UPDATED_TOPIC_NAME, Produced.with(Serdes.String(), new BalanceUpdatedEventSerde()));

            return resultKStream;
        };
    }

    private TransactionCreatedEventSet updateTransactionCreatedEventList(TransactionCreatedEvent event) {
        ReadOnlyKeyValueStore<String, TransactionCreatedEventSet> keyValueStore = interactiveQueryService.getQueryableStore(eventStoreName, QueryableStoreTypes.keyValueStore());
        TransactionCreatedEventSet eventSet = keyValueStore.get(event.getWalletId());
        if(eventSet != null) {
            LOGGER.info("Event Store size is " + eventSet.getEventSet().size());
            eventSet.getEventSet().forEach(x -> LOGGER.info(x.toString()));
            eventSet.getEventSet().add(event);
        } else {
            LOGGER.info("Event Store is empty, key=" + event.getWalletId());
            eventSet = new TransactionCreatedEventSet();
            eventSet.getEventSet().add(event);
        }
        return eventSet;
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
