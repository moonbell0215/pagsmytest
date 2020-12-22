package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class TransactionCreatedEvent {
    private final String transactionId;
    private final double transactionAmount;
    private final String walletId;
    private final Date transactionDateTime;
    private final TransactionType transactionType;
    private final String description;

    @JsonCreator
    public TransactionCreatedEvent(@JsonProperty("transactionId") String transactionId,
                                   @JsonProperty("transactionAmount") double transactionAmount,
                                   @JsonProperty("walletId") String walletId,
                                   @JsonProperty("transactionDateTime") Date transactionDateTime,
                                   @JsonProperty("transactionType") TransactionType transactionType,
                                   @JsonProperty("description") String description) {
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.walletId = walletId;
        this.transactionDateTime = transactionDateTime;
        this.transactionType = transactionType;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("id")
    @JsonIgnore
    public String getId() {
        return transactionId;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getWalletId() {
        return walletId;
    }

    public Date getTransactionDateTime() {
        return transactionDateTime;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getDescription() {
        return description;
    }
}
