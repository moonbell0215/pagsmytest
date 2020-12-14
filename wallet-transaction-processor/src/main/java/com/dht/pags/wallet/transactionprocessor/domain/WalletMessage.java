package com.dht.pags.wallet.transactionprocessor.domain;

import java.io.Serializable;


public class WalletMessage implements Serializable {
    private static final long serialVersionUID = 6678420965611108427L;

    private String userId;

    private String message;

    public WalletMessage() {
    }

    public WalletMessage(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    @Override
    public String toString() {
        return "WalletMessage{" +
                "userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
