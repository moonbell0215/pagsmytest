package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BalanceUpdatedEvent {
    /**
     * 本次交易ID
     */
    private final String id;
    /**
     * 本次交易的金額
     */
    private final BigDecimal transactionAmount;
    /**
     * 交易錢包ID ，(會員ID)
     */
    private final String walletId;
    /**
     * 交易時間
     */
    private final long updateTime;
    /**
     * 交易後餘額，當前餘額
     */
    private final BigDecimal balance;
    /**
     * 交易餘額
     */
    private final BigDecimal beforeBalance;


    @JsonCreator
    public BalanceUpdatedEvent(@JsonProperty("id") String id,
                               @JsonProperty("transactionAmount") BigDecimal transactionAmount,
                               @JsonProperty("walletId") String walletId,
                               @JsonProperty("updateTime") long updateTime,
                               @JsonProperty("balance") BigDecimal balance,
                               @JsonProperty("previousBalance") BigDecimal previousBalance) {
        this.id = id;
        this.transactionAmount = transactionAmount;
        this.walletId = walletId;
        this.updateTime = updateTime;
        this.balance = balance;
        this.beforeBalance = previousBalance;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getBeforeBalance() { return beforeBalance; }

    public long getUpdateTime() { return updateTime; }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TransactionCreatedEvent{");
        sb.append("id='").append(id).append('\'');
        sb.append(", transactionAmount=").append(transactionAmount);
        sb.append(", walletId='").append(walletId + '\'');
        sb.append(", updateTime=").append(updateTime);
        sb.append(", balance=").append(balance);
        sb.append(", beforeBalance=").append(beforeBalance);
        sb.append('}');
        return sb.toString();
    }
}
