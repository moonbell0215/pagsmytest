package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CreateTransactionCommandProcessedEvent {
    /**
     * 本次交易ID  成功才會有交易ID
     */
    private final String id;
    /**
     * 本次交易金額
     */
    private final BigDecimal transactionAmount;
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
     * 操作人員
     */
    private final OperatorType operatorType;
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
                                                  @JsonProperty("transactionAmount") BigDecimal transactionAmount,
                                                  @JsonProperty("orderId") String orderId,
                                                  @JsonProperty("walletId") String walletId,
                                                  @JsonProperty("transactionDateTime") long transactionDateTime,
                                                  @JsonProperty("transactionType") TransactionType transactionType,
                                                  @JsonProperty("operatorType") OperatorType operatorType,
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
        this.operatorType = operatorType;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getTransactionAmount() {
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

    public OperatorType getOperatorType() {
        return operatorType;
    }

    @Override
    public String toString() {
        return "CreateTransactionCommandProcessedEvent{" +
                "id='" + id + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", orderId='" + orderId + '\'' +
                ", walletId='" + walletId + '\'' +
                ", transactionDateTime=" + transactionDateTime +
                ", transactionType=" + transactionType +
                ", operatorType=" + operatorType +
                ", description='" + description + '\'' +
                ", transactionStatus=" + transactionStatus +
                '}';
    }
}
