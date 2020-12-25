package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class CreateTransactionCommand {
    private final String orderId;
    private final double orderAmount;
    private final String walletId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final Date orderDateTime;
    private final TransactionType transactionType;
    private final String description;

    @JsonCreator
    public CreateTransactionCommand(@JsonProperty("transactionId") String orderId,
                                    @JsonProperty("transactionAmount") double transactionAmount,
                                    @JsonProperty("walletId") String walletId,
                                    @JsonProperty("transactionDateTime") Date orderDateTime,
                                    @JsonProperty("transactionType") TransactionType transactionType,
                                    @JsonProperty("description") String description) {
        this.orderId = orderId;
        this.orderAmount = transactionAmount;
        this.walletId = walletId;
        this.orderDateTime = orderDateTime;
        this.transactionType = transactionType;
        this.description = description;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public String getWalletId() {
        return walletId;
    }

    public Date getOrderDateTime() {
        return orderDateTime;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "CreateTransactionCommand{" +
                "orderId='" + orderId + '\'' +
                ", orderAmount=" + orderAmount +
                ", walletId='" + walletId + '\'' +
                ", orderDateTime=" + orderDateTime +
                ", transactionType=" + transactionType +
                ", description='" + description + '\'' +
                '}';
    }
}
