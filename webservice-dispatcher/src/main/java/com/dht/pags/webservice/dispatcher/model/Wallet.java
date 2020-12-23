package com.dht.pags.webservice.dispatcher.model;

import java.math.BigDecimal;

public class Wallet {

    private String employeecode;

    private String currency;

    private BigDecimal balance;

    private Long updatetime;

    public String getEmployeecode() {
        return employeecode;
    }

    public void setEmployeecode(String employeecode) {
        this.employeecode = employeecode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Long updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "employeecode='" + employeecode + '\'' +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                ", updatetime=" + updatetime +
                '}';
    }
}