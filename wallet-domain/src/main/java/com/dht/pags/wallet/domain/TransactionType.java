package com.dht.pags.wallet.domain;

public enum TransactionType {

    /**
     * 手动冲正，需要设定流水倍数
     * 加钱
     */
    INCREASE(false),
    /**
     * 手动冲负，需要设定流水倍数
     * 扣钱
     */
    DECREASE(true),
    /**
     * 存款，默认1倍流水
     * 加钱
     */
    DEPOSIT(false),
    /**
     * 取款申请
     * 扣钱
     */
    WITHDRAW_APPLY(true),
    /**
     * 取款拒绝
     * 加钱
     */
    WITHDRAW_REJECT(false),
    /**
     * 游戏下注
     * 扣钱
     */
    BET_PLACE(true),
    /**
     * 取消下注
     * 加钱
     */
    BET_CANCLE(false),
    /**
     * 派彩
     */
    PAYOUT(false),
    /**
     * 派彩加钱--重新結算
     */
    RE_PAYOUT_ADD(false),
    /**
     * 派彩扣钱--重新結算
     */
    RE_PAYOUT(true),
    ;

    private final boolean isReduce;

    TransactionType(boolean isReduce){
        this.isReduce = isReduce;
    }

    public boolean isReduce() {
        return isReduce;
    }
}
