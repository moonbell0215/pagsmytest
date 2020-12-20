package com.dht.pags.wallet.transactionprocessor;

import java.util.Date;

public class WordCount {
    private Long key;
    private String value;

    public WordCount() {
    }

    public WordCount(Long key, String value) {
        this.key = key;
        this.value = value;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value.toUpperCase();
    }
}
