package com.dht.pags.wallet.transactionprocessor.fn;

import java.util.Date;

public class WordCount {
    private String key;
    private Long value;
    private Date start;
    private Date end;

    public WordCount(String key, Long value, Date start, Date end) {
        this.key = key;
        this.value = value;
        this.start = start;
        this.end = end;
    }
}
