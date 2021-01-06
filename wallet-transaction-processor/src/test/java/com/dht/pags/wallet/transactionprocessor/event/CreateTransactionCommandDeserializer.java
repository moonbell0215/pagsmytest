package com.dht.pags.wallet.transactionprocessor.event;


import com.alibaba.fastjson.JSON;
import com.dht.pags.wallet.domain.CreateTransactionCommand;
import org.apache.kafka.common.serialization.Deserializer;

public class CreateTransactionCommandDeserializer implements Deserializer<CreateTransactionCommand> {

    @Override
    public CreateTransactionCommand deserialize(String topic, byte[] data) {
        return JSON.parseObject(data ,CreateTransactionCommand.class);
    }
}
