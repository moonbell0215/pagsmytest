package com.dht.pags.wallet.transactionprocessor.fn;

import com.dht.pags.wallet.domain.WordCount;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class DemoFunction {

    /**
     * This is example to be removed
     **/
    @Bean
    public Function<KStream<String, WordCount>, KStream<Long, String>> uppercase() {
        return input -> input
                .map((key, value) -> new KeyValue<Long,String>(Long.valueOf(key), value.getValue()));
    }
}
