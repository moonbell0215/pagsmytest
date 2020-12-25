package com.dht.pags.wallet.webservice.inquiryprocessor.domain;

import java.math.BigDecimal;

public class Transaction {
    public Transaction() {
    }

    private String orderId;
    private String walletId;
    private String paymentTypeCode;
    private Long transactionDatetime;
    private BigDecimal transactionAmount;
    private String currency;
    private int transactionType;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getPaymentTypeCode() {
        return paymentTypeCode;
    }

    public void setPaymentTypeCode(String paymentTypeCode) {
        this.paymentTypeCode = paymentTypeCode;
    }

    public Long getTransactionDatetime() {
        return transactionDatetime;
    }

    public void setTransactionDatetime(Long transactionDatetime) {
        this.transactionDatetime = transactionDatetime;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }
}
