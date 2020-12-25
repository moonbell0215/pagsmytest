package com.dht.pags.wallet.transactionprocessor.fn;

import com.dht.pags.wallet.domain.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.BiFunction;

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
    public BiFunction<KStream<String, CreateTransactionCommand>,
            KStream<String, TransactionCreatedEvent>,
            KStream<String, CreateTransactionCommandProcessedEvent>> createTransactionCommandHandler() {
        return (stringCreateTransactionCommandKStream,transactionCreatedEventKStream) -> {


            KStream<String, CreateTransactionCommandProcessedEvent> createTransactionCommandProcessedEventKStream =
                    stringCreateTransactionCommandKStream.peek((k, v) -> LOGGER.info("CreateTransactionCommand: " + v))
                            // out -> wallet.createTransactionCommandProcessedEvent 申請帳變記錄 ->CreateTransactionCommandProcessedEvent
                            .map((key, create) -> new KeyValue<>(key, validateAndCreateTransactionCreatedEvent(create)));

            //out -> wallet.transactionCreatedEvent 帳變交易記錄 -> TransactionCreatedEvent
            createTransactionCommandProcessedEventKStream
                    .filter((key, processedEvent) -> TransactionStatus.SUCCESS.equals(processedEvent.getTransactionStatus()))
                    .map((key, create) -> new KeyValue<>(key, createTransactionEvent(create)))
                    .peek((k, v) -> LOGGER.info("transactionCreatedEvent: " + v))
                    .to(SUCCESS_TRANSACTION_TOPIC_NAME, Produced.with(Serdes.String(), new TransactionCreatedEventSerde()))
            ;

            //out -> wallet.balanceUpdatedEvent 餘額記錄 -> BalanceUpdatedEvent
            //會在wallet.transactionCreatedEvent有輸出後，才會觸發此項
            transactionCreatedEventKStream.peek((k, v) -> LOGGER.info("balanceUpdatedEvent: " + v))
                    .map((key, create) -> new KeyValue<>(key, createBalanceUpdatedEvent(create)))
                    .to(BALANCE_UPDATED_TOPIC_NAME, Produced.with(Serdes.String(), new BalanceUpdatedEventSerde()));

            //in  -> wallet.createTransactionCommand  輸入申請帳變 -> CreateTransactionCommand
            return createTransactionCommandProcessedEventKStream.peek((k, v) -> LOGGER.info("createTransactionCommandProcessedEventKStream: " + v));
        };
    }
//    @Bean
//    public Function<KStream<String, CreateTransactionCommand>, KStream<String, TransactionCreatedEvent>> createTransactionCommandHandler() {
//        return input -> input
//                .map((key, createTransactionCommand) -> new KeyValue(key, validateAndCreateTransactionCreatedEvent(createTransactionCommand)));
//    }

    private CreateTransactionCommandProcessedEvent validateAndCreateTransactionCreatedEvent(CreateTransactionCommand createTransactionCommand) {
        if (this.validateCreateTransactionCommand(createTransactionCommand)) {
            return createTransactionCommandProcessedEvent(createTransactionCommand,TransactionStatus.SUCCESS);
        }
        //TODO : Return error
        return createTransactionCommandProcessedEvent(createTransactionCommand,TransactionStatus.FAILURE);
    }

    private boolean validateCreateTransactionCommand(CreateTransactionCommand createTransactionCommand) {
        //TODO: Implement validation logic
        //SUCCESS orderAmount 大於 100
        if(createTransactionCommand.getOrderAmount() >100 ){
            return true;
        }else{
        //test FAILURE orderAmount 小於等於 100
            return false;
        }

    }

    private TransactionCreatedEvent createTransactionEvent(CreateTransactionCommandProcessedEvent createTransactionCommandProcessedEvent) {
        //TODO: Implement logic
        return new TransactionCreatedEvent(createTransactionCommandProcessedEvent.getTransactionId(),
                createTransactionCommandProcessedEvent.getTransactionAmount(),
                createTransactionCommandProcessedEvent.getWalletId(),
                createTransactionCommandProcessedEvent.getTransactionDateTime(),
                createTransactionCommandProcessedEvent.getTransactionType(),
                "Note by event:" + createTransactionCommandProcessedEvent.getDescription()
        );
    }

    private BalanceUpdatedEvent createBalanceUpdatedEvent(TransactionCreatedEvent transactionCreatedEvent) {
        //TODO: Implement logic
        return new BalanceUpdatedEvent(transactionCreatedEvent.getId(),
                transactionCreatedEvent.getTransactionAmount(),
                transactionCreatedEvent.getWalletId(),
                transactionCreatedEvent.getTransactionDateTime(),
                transactionCreatedEvent.getTransactionType(),
                "Note by event:" + transactionCreatedEvent.getDescription()
        );
    }
    private CreateTransactionCommandProcessedEvent createTransactionCommandProcessedEvent(CreateTransactionCommand createTransactionCommand, TransactionStatus transactionStatus) {
        //TODO: Implement logic
        return new CreateTransactionCommandProcessedEvent(createTransactionCommand.getOrderId(),
                createTransactionCommand.getOrderAmount(),
                createTransactionCommand.getWalletId(),
                System.currentTimeMillis(),
                createTransactionCommand.getTransactionType(),
                "Note by event:" + createTransactionCommand.getDescription(),
                transactionStatus);
    }
}
