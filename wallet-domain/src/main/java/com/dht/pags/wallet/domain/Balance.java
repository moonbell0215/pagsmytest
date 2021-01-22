package com.dht.pags.wallet.domain;

import java.math.BigDecimal;

public class Balance {
    public Balance() {
    }

    public Balance(String id, String walletId, BigDecimal balance, Long updateTime) {
        this.id = id;
        this.walletId = walletId;
        this.balance = balance;
        this.updateTime = updateTime;
    }

    private String id;
    private String walletId;
    private BigDecimal balance;
    private Long updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }


}
