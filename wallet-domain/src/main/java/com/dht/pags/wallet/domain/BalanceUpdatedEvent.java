package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

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
     * 交易前餘額
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BalanceUpdatedEvent that = (BalanceUpdatedEvent) o;
        return updateTime == that.updateTime && Objects.equals(id, that.id) && Objects.equals(transactionAmount, that.transactionAmount) && Objects.equals(walletId, that.walletId) && Objects.equals(balance, that.balance) && Objects.equals(beforeBalance, that.beforeBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionAmount, walletId, updateTime, balance, beforeBalance);
    }

    @Override
    public String toString() {
        return "BalanceUpdatedEvent{" +
                "id='" + id + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", walletId='" + walletId + '\'' +
                ", updateTime=" + updateTime +
                ", balance=" + balance +
                ", beforeBalance=" + beforeBalance +
                '}';
    }
}
