package com.dht.pags.wallet.webservice.inquiryprocessor.domain;

import java.math.BigDecimal;

import java.util.Date;

public class Balance {
    public Balance() {
    }

    private BigDecimal balance;
    private Long updateTime;

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
