package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class TransactionCreatedEvent {

    /**
     * 本次交易ID  成功才會有交易ID
     */
    private final String id;
    /**
     * 會員訂單編號
     */
    private final String orderId;
    /**
     * 本次交易金額
     */
    private final BigDecimal transactionAmount;
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
     * 操作人員
     */
    private final OperatorType operatorType;

    /**
     * 交易說明
     */
    private final String description;

    @JsonCreator
    public TransactionCreatedEvent(@JsonProperty("id") String id,
                                   @JsonProperty("orderId") String orderId,
                                   @JsonProperty("transactionAmount") BigDecimal transactionAmount,
                                   @JsonProperty("walletId") String walletId,
                                   @JsonProperty("transactionDateTime") long transactionDateTime,
                                   @JsonProperty("transactionType") TransactionType transactionType,
                                   @JsonProperty("operatorType") OperatorType operatorType,
                                   @JsonProperty("description") String description) {
        this.id = id;
        this.orderId = orderId;
        this.transactionAmount = transactionAmount;
        this.walletId = walletId;
        this.transactionDateTime = transactionDateTime;
        this.transactionType = transactionType;
        this.operatorType = operatorType;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() { return orderId; }

    public BigDecimal getTransactionAmount() {
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

    public OperatorType getOperatorType() {
        return operatorType;
    }

    @Override
    public String toString() {
        return "TransactionCreatedEvent{" +
                "id='" + id + '\'' +
                ", orderId='" + orderId + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", walletId='" + walletId + '\'' +
                ", transactionDateTime=" + transactionDateTime +
                ", transactionType=" + transactionType +
                ", operatorType=" + operatorType +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCreatedEvent that = (TransactionCreatedEvent) o;
        return that.transactionAmount.compareTo(transactionAmount) == 0 && transactionDateTime == that.transactionDateTime && Objects.equals(id, that.id) && Objects.equals(orderId, that.orderId) && Objects.equals(walletId, that.walletId) && transactionType == that.transactionType && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, transactionAmount, walletId, transactionDateTime, transactionType, description);
    }
}
