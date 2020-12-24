package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class CreateTransactionCommandProcessedEvent {
    private final String transactionId;
    private final double transactionAmount;
    private final String walletId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final Date transactionDateTime;
    private final TransactionType transactionType;
    private final String description;

    @JsonCreator
    public CreateTransactionCommandProcessedEvent(@JsonProperty("transactionId") String transactionId,
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

    @Override
    public String toString() {
        return "CreateTransactionCommandProcessedEvent{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", walletId='" + walletId + '\'' +
                ", transactionDateTime=" + transactionDateTime +
                ", transactionType=" + transactionType +
                ", description='" + description + '\'' +
                '}';
    }
}
