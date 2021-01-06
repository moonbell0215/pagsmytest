package com.dht.pags.wallet.transactionprocessor.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dht.pags.wallet.domain.CreateTransactionCommandProcessedEvent;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTransactionCommandProcessedEventSerializer implements Serializer<CreateTransactionCommandProcessedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTransactionCommandSerializer.class);

    @Override
    public byte[] serialize(String topic, CreateTransactionCommandProcessedEvent data) {
        LOGGER.info("topic="+topic + data.toString());
        return JSON.toJSONBytes(JSON.parseObject(JSON.toJSONString(data),JSONObject.class));
    }
    
}
