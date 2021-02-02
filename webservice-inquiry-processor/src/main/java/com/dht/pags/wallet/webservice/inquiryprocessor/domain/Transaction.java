package com.dht.pags.wallet.webservice.inquiryprocessor.domain;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    public Transaction() {
    }

    private String id;
    private String orderId;
    private String walletId;
    private String transactionDateTime;
    private BigDecimal transactionAmount;
    private String operatorType;
    private String transactionType;
    private String description;

    protected static final String DEFAULT_DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(long transactionDateTime) {
        Date date = new Date(transactionDateTime);
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DB_DATE_FORMAT);
        this.transactionDateTime = format.format(date);
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
