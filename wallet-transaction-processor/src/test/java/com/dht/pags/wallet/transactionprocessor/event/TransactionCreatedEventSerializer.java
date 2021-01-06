package com.dht.pags.wallet.transactionprocessor.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.apache.kafka.common.serialization.Serializer;

public class TransactionCreatedEventSerializer implements Serializer<TransactionCreatedEvent> {

    @Override
    public byte[] serialize(String topic, TransactionCreatedEvent data) {
        return JSON.toJSONBytes(JSON.parseObject(JSON.toJSONString(data),JSONObject.class));
    }
    
}
