package com.dht.pags.wallet.transactionprocessor.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dht.pags.wallet.domain.BalanceUpdatedEvent;
import org.apache.kafka.common.serialization.Serializer;

public class BalanceUpdatedEventSerializer implements Serializer<BalanceUpdatedEvent> {

    @Override
    public byte[] serialize(String topic, BalanceUpdatedEvent data) {
        return JSON.toJSONBytes(JSON.parseObject(JSON.toJSONString(data),JSONObject.class));
    }
    
}
