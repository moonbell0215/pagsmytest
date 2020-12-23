package com.dht.pags.wallet.domain;

public enum TransactionType {
    /**
     * 手动冲正，需要设定流水倍数
     * 加钱
     */
    INCREASE,
    /**
     * 手动冲负，需要设定流水倍数
     * 扣钱
     */
    DECREASE,
    /**
     * 存款，默认1倍流水
     * 加钱
     */
    DEPOSIT,
    /**
     * 取款申请
     * 扣钱
     */
    WITHDRAW_APPLY,
    /**
     * 取款拒绝
     * 加钱
     */
    WITHDRAW_REJECT,
    /**
     * 游戏下注
     * 扣钱
     */
    BET_PLACE,
    /**
     * 取消下注
     * 加钱
     */
    BET_CANCLE,
    /**
     * 派彩
     */
    PAYOUT,


    ;
}
