package com.dht.pags.wallet.webservice.inquiryprocessor.domain;

import java.math.BigDecimal;

public class Transaction {
    public Transaction() {
    }

    private String orderId;
    private String walletId;
    private Long transactionDateTime;
    private BigDecimal transactionAmount;
    private String operatorType;
    private String transactionType;
    private String description;

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

    public Long getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(Long transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
