package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionCreatedEvent {
    /**
     * 本次交易ID  成功才會有交易ID
     */
    private final String transactionId;
    /**
     * 會員訂單編號
     */
    private final String orderId;
    /**
     * 本次交易金額
     */
    private final double transactionAmount;
    /**
     * 會員錢包id
     */
    private final String walletId;
    /**
     * 交易時間
     */
    private final long transactionDateTime;
    /**
     * 交易類別
     */
    private final TransactionType transactionType;
    /**
     * 交易說明
     */
    private final String description;

    @JsonCreator
    public TransactionCreatedEvent(@JsonProperty("transactionId") String transactionId,
                                   @JsonProperty("orderId") String orderId,
                                   @JsonProperty("transactionAmount") double transactionAmount,
                                   @JsonProperty("walletId") String walletId,
                                   @JsonProperty("transactionDateTime") long transactionDateTime,
                                   @JsonProperty("transactionType") TransactionType transactionType,
                                   @JsonProperty("description") String description) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.transactionAmount = transactionAmount;
        this.walletId = walletId;
        this.transactionDateTime = transactionDateTime;
        this.transactionType = transactionType;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOrderId() { return orderId; }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getWalletId() {
        return walletId;
    }

    public long getTransactionDateTime() {
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
        final StringBuffer sb = new StringBuffer("TransactionCreatedEvent{");
        sb.append("transactionId='").append(transactionId).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", transactionAmount=").append(transactionAmount);
        sb.append(", walletId='").append(walletId).append('\'');
        sb.append(", transactionDateTime=").append(transactionDateTime);
        sb.append(", transactionType=").append(transactionType);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
