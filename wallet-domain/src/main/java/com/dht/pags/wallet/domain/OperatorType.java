package com.dht.pags.wallet.domain;

/**
 * 操作人員類型
 */
public enum OperatorType {
    /**
     * 後台 員工
     */
    STAFF,
    /**
     * 前台 投注
     */
    API_BET,
    /**
     * //前台 第三方支付 Third-party payment
     */
    API_THIRD_PARTY_PAYMENT,
    /**
     * //排程 schedule
     */
    SCHEDULE,
    ;
}
