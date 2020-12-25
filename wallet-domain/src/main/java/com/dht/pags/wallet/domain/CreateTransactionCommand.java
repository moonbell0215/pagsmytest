package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class CreateTransactionCommand {
    /**
     * 會員訂單編號
     */
    private final String orderId;
    /**
     * 會員訂單金額
     */
    private final double orderAmount;
    /**
     * 會員錢包id
     */
    private final String walletId;
    /**
     * 交易類型
     */
    private final TransactionType transactionType;
    /**
     * 交易說明
     */
    private final String description;
    //TODO-再討論要傳入參數，以上是目前想到必要的參數值

    @JsonCreator
    public CreateTransactionCommand(@JsonProperty("orderId") String orderId,
                                    @JsonProperty("orderAmount") double orderAmount,
                                    @JsonProperty("walletId") String walletId,
                                    @JsonProperty("transactionType") TransactionType transactionType,
                                    @JsonProperty("description") String description) {
        this.orderId = orderId;
        this.orderAmount = orderAmount;
        this.walletId = walletId;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getDescription() { return description; }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CreateTransactionCommand{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", orderAmount=").append(orderAmount);
        sb.append(", walletId='").append(walletId).append('\'');
        sb.append(", transactionType=").append(transactionType);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
