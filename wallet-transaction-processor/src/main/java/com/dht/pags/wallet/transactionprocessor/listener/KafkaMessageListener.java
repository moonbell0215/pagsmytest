package com.dht.pags.wallet.transactionprocessor.listener;

import com.dht.pags.wallet.transactionprocessor.domain.WalletMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class KafkaMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @KafkaListener(topics = "test", groupId = "test-consumer")
    public void listen(WalletMessage walletMessage) {
        logger.info("GET Wallet Message: {}", walletMessage);
    }

}
