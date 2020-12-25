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
    private final TransactionStatus transactionStatus;

    @JsonCreator
    public CreateTransactionCommandProcessedEvent(@JsonProperty("transactionId") String transactionId,
                                                  @JsonProperty("transactionAmount") double transactionAmount,
                                                  @JsonProperty("walletId") String walletId,
                                                  @JsonProperty("transactionDateTime") Date transactionDateTime,
                                                  @JsonProperty("transactionType") TransactionType transactionType,
                                                  @JsonProperty("description") String description,
                                                  @JsonProperty("transactionStatus") TransactionStatus transactionStatus) {
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.walletId = walletId;
        this.transactionDateTime = transactionDateTime;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionStatus = transactionStatus;
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

    public TransactionStatus getTransactionStatus() { return transactionStatus; }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CreateTransactionCommandProcessedEvent{");
        sb.append("transactionId='").append(transactionId).append('\'');
        sb.append(", transactionAmount=").append(transactionAmount);
        sb.append(", walletId='").append(walletId).append('\'');
        sb.append(", transactionDateTime=").append(transactionDateTime);
        sb.append(", transactionType=").append(transactionType);
        sb.append(", description='").append(description).append('\'');
        sb.append(", transactionStatus=").append(transactionStatus);
        sb.append('}');
        return sb.toString();
    }
}
