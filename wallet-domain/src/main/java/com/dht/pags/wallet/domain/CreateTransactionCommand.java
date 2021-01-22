package com.dht.pags.wallet.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CreateTransactionCommand {
    /**
     * 會員訂單編號
     */
    private final String orderId;
    /**
     * 會員訂單金額
     */
    private final BigDecimal orderAmount;
    /**
     * 會員錢包id
     */
    private final String walletId;
    /**
     * 交易類型
     */
    private final TransactionType transactionType;
    /**
     * 操作人員
     */
    private final OperatorType operatorType;
    /**
     * 交易說明
     */
    private final String description;



    //TODO-再討論要傳入參數，以上是目前想到必要的參數值

    @JsonCreator
    public CreateTransactionCommand(@JsonProperty("orderId") String orderId,
                                    @JsonProperty("orderAmount") BigDecimal orderAmount,
                                    @JsonProperty("walletId") String walletId,
                                    @JsonProperty("transactionType") TransactionType transactionType,
                                    @JsonProperty("operatorType") OperatorType operatorType,
                                    @JsonProperty("description") String description
                                    ) {
        this.orderId = orderId;
        this.walletId = walletId;
        this.transactionType = transactionType;
        if(this.transactionType.isReduce())
        {
            this.orderAmount = new BigDecimal("0").subtract(orderAmount.abs());
        }
        else
        {
            this.orderAmount = orderAmount.abs();
        }
        this.description = description;
        this.operatorType = operatorType;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public String getWalletId() {
        return walletId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getDescription() { return description; }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    @Override
    public String toString() {
        return "CreateTransactionCommand{" +
                "orderId='" + orderId + '\'' +
                ", orderAmount=" + orderAmount +
                ", walletId='" + walletId + '\'' +
                ", transactionType=" + transactionType +
                ", description='" + description + '\'' +
                ", operatorType=" + operatorType +
                '}';
    }
}
