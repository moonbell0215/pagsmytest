package com.dht.pags.wallet.transactionprocessor.domain;

import java.util.Date;

public class CreateTransactionCommand {
    private final String transactionId;
    private final double transactionAmount;
    private final String walletId;
    private final Date transactionDateTime;
    private final TransactionType transactionType;
    private final String description;

    public CreateTransactionCommand(String transactionId, double transactionAmount, String walletId, Date transactionDateTime, TransactionType transactionType, String description) {
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
}
