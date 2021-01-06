package com.dht.pags.wallet.transactionprocessor.event;


import com.alibaba.fastjson.JSON;
import com.dht.pags.wallet.domain.BalanceUpdatedEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class BalanceUpdatedEventDeserializer implements Deserializer<BalanceUpdatedEvent> {

    @Override
    public BalanceUpdatedEvent deserialize(String topic, byte[] data) {
        return JSON.parseObject(data , BalanceUpdatedEvent.class);
    }
}
