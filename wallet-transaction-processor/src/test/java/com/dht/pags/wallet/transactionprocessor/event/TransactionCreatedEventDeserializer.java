package com.dht.pags.wallet.transactionprocessor.event;


import com.alibaba.fastjson.JSON;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class TransactionCreatedEventDeserializer implements Deserializer<TransactionCreatedEvent> {

    @Override
    public TransactionCreatedEvent deserialize(String topic, byte[] data) {
        return JSON.parseObject(data ,TransactionCreatedEvent.class);
    }
}
