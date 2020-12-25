package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTransactionCommandProcessedEvent {
    /**
     * 本次交易ID  成功才會有交易ID
     */
    private final String id;
    /**
     * 本次交易金額
     */
    private final double transactionAmount;
    /**
     * 會員訂單編號
     */
    private final String orderId;
    /**
     * 交易錢包ID ，(會員ID)
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
    /**
     * 交易狀態
     */
    private final TransactionStatus transactionStatus;

    @JsonCreator
    public CreateTransactionCommandProcessedEvent(@JsonProperty("id") String id,
                                                  @JsonProperty("transactionAmount") double transactionAmount,
                                                  @JsonProperty("orderId") String orderId,
                                                  @JsonProperty("walletId") String walletId,
                                                  @JsonProperty("transactionDateTime") long transactionDateTime,
                                                  @JsonProperty("transactionType") TransactionType transactionType,
                                                  @JsonProperty("description") String description,
                                                  @JsonProperty("transactionStatus") TransactionStatus transactionStatus) {
        this.id = id;
        this.transactionAmount = transactionAmount;
        this.orderId = orderId;
        this.walletId = walletId;
        this.transactionDateTime = transactionDateTime;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionStatus = transactionStatus;
    }

    public String getId() {
        return id;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getOrderId() { return orderId; }

    public long getTransactionDateTime() {
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
        sb.append("id='").append(id).append('\'');
        sb.append(", transactionAmount=").append(transactionAmount);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", walletId='").append(walletId).append('\'');
        sb.append(", transactionDateTime=").append(transactionDateTime);
        sb.append(", transactionType=").append(transactionType);
        sb.append(", description='").append(description).append('\'');
        sb.append(", transactionStatus=").append(transactionStatus);
        sb.append('}');
        return sb.toString();
    }
}
