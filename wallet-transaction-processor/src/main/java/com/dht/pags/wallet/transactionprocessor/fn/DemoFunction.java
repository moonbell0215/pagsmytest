package com.dht.pags.wallet.transactionprocessor.fn;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class DemoFunction {

    /** This is example to be removed **/
    @Bean
    public Function<String, String> uppercase() {
        return value -> {
            System.out.println("Received: " + value);
            return value.toUpperCase();
        };
    }

}
