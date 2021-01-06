package com.dht.pags.wallet.transactionprocessor.event;


import com.alibaba.fastjson.JSON;
import com.dht.pags.wallet.domain.CreateTransactionCommandProcessedEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class CreateTransactionCommandProcessedEventDeserializer implements Deserializer<CreateTransactionCommandProcessedEvent> {

    @Override
    public CreateTransactionCommandProcessedEvent deserialize(String topic, byte[] data) {
        return JSON.parseObject(data ,CreateTransactionCommandProcessedEvent.class);
    }
}
