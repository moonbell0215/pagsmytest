package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class CreateTransactionCommand {
    private final String orderId;
    private final double orderAmount;
    private final String walletId;
    private final long orderDateTime;
    private final TransactionType transactionType;
    private final String description;

    @JsonCreator
    public CreateTransactionCommand(@JsonProperty("orderId") String orderId,
                                    @JsonProperty("orderAmount") double orderAmount,
                                    @JsonProperty("walletId") String walletId,
                                    @JsonProperty("orderDateTime") long orderDateTime,
                                    @JsonProperty("transactionType") TransactionType transactionType,
                                    @JsonProperty("description") String description) {
        this.orderId = orderId;
        this.orderAmount = orderAmount;
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

    public long getOrderDateTime() {
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
