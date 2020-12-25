package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BalanceUpdatedEvent {
    /**
     * 本次交易ID
     */
    private final String transactionId;
    /**
     * 本次交易的金額
     */
    private final double transactionAmount;
    /**
     * 交易錢包ID ，(會員ID)
     */
    private final String walletId;
    /**
     * 交易後餘額，當前餘額
     */
    private final double balance;


    @JsonCreator
    public BalanceUpdatedEvent(@JsonProperty("transactionId") String transactionId,
                               @JsonProperty("transactionAmount") double transactionAmount,
                               @JsonProperty("walletId") String walletId,
                               @JsonProperty("balance")  double balance) {
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.walletId = walletId;
        this.balance = balance;
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

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BalanceUpdatedEvent{");
        sb.append("transactionId='").append(transactionId).append('\'');
        sb.append(", transactionAmount=").append(transactionAmount);
        sb.append(", walletId='").append(walletId).append('\'');
        sb.append(", balance=").append(balance);
        sb.append('}');
        return sb.toString();
    }
}
