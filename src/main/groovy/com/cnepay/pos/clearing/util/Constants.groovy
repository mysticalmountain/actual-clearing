package com.cnepay.pos.clearing.util

/**
 * User: andongxu
 * Time: 15-8-24 下午3:31 
 */
class Constants {

    /**
     *  清分状态 -处理中
     */
    public static final String CLEAR_STATUS_PROCESS = "1"
    /**
     * 清分状态-成功
     */
    public static final String CLEARING_STATUS_SUCCESS = "2"
    /**
     * 清分状态-失败
     */
    public static final String CLEARING_STATUS_FAIL = "3"

    /** *****************************************************************************************************************/

    /**
     *  记账状态 -处理中
     */
    public static final String ACCOUNT_STATUS_PROCESS = "1"
    /**
     * 记账状态-成功
     */
    public static final String ACCOUNT_STATUS_SUCCESS = "2"
    /**
     * 记账状态-失败
     */
    public static final String ACCOUNT_STATUS_FAIL = "3"
    /**
     * 记账状态-请求/响应超时
     */
    public static final String ACCOUNT_STATUS_TIMEOUT = "4"

    /** *****************************************************************************************************************/


    /**
     *  解冻状态 -处理中
     */
    public static final String UNFREEZE_STATUS_PROCESS = "1"
    /**
     * 解冻状态-成功
     */
    public static final String UNFREEZE_STATUS_SUCCESS = "2"
    /**
     * 解冻状态-失败
     */
    public static final String UNFREEZE_STATUS_FAIL = "3"
    /**
     * 解冻状态-请求/响应超时
     */
    public static final String UNFREEZE_STATUS_TIMEOUT = "4"

    /** *****************************************************************************************************************/


    /**
     * 交易状态-成功
     */
    public static final String TRANS_STATUS_SUCCESS = "1"
    /**
     * 交易状态-已冲正
     */
    public static final String TRANS_STATUS_REVERSE = "2"
    /**
     * 交易状态-已冲正
     */
    public static final String TRANS_STATUS_NOTSUCCESS = "0"
    /**
     * 交易状态-已撤销
     */
    public static final String TRANS_STATUS_REVOKE = "3"
    /**
     * 交易状态-已退款
     */
    public static final String TRANS_STATUS_REFUND = "4"

    /** *****************************************************************************************************************/

    /**
     * 交易类型-消费
     */
    public static final String TRANS_TYPE_SALE = "sale"
    /**
     * 交易类型-冲正
     */
    public static final String TRANS_TYPE_REVERSAL_SALE = "reversal_sale"
    /**
     * 消交易类型-消费撤销
     */
    public static final String TRANS_TYPE_SALE_VOID = "sale_void"
    /**
     * 消交易类型-消费撤销冲正
     */
    public static final String TRANS_TYPE_REVERSAL_SALE_VOID = "reversal_sale_void"
    /**
     * 消交易类型-预授权完成
     */
    public static final String TRANS_TYPE_AUTH_COMP = "auth_comp"
    /**
     * 消交易类型-预授权完成冲正
     */
    public static final String TRANS_TYPE_REVERSAL_AUTH_COMP = "reversal_auth_comp"
    /**
     * 消交易类型-预授权完成撤销
     */
    public static final String TRANS_TYPE_AUTH_COMP_CANCEL = "auth_comp_cancel"
    /**
     * 消交易类型-预授权完成撤销冲正
     */
    public static final String TRANS_TYPE_REVERSAL_AUTH_COMP_CANCEL = "reversal_auth_comp_cancel"
    /**
     * 退款
     */
    public static final String TRANS_TYPE_REFUND = "refund"

    /** *****************************************************************************************************************/

    /**
     *  卡类型-借记卡
     */
    public static final String CARD_TYPE_DEBIT = "debit"
    /**
     * 卡类型-信用卡
     */
    public static final String CARD_TYPE_DREDIT = "dredit"

    /** *****************************************************************************************************************/

    /**
     * 通道代码-银联
     */
    public static final String ACQUIRER_CODE_UPOP = "unionpay"
    /**
     * 通道代码-北京浦发
     */
    public static final String ACQUIRER_CODE_BJSPDB = "unionpay"

    /** *****************************************************************************************************************/

    /**
     * 手续费费率设置-费率
     */
    public static final int FEE_SETTINGS_RATE = 11
    /**
     * 手续费费率设置-定值
     */
    public static final int FEE_SETTINGS_FIXED = 12
    /**
     * 手续费费率设置-封顶
     */
    public static final int FEE_SETTINGS_CAP = 12

    /** *****************************************************************************************************************/


     /**
     *  渠道号 - 收单
     */
    public static final String CHANNEL_POS = "POSP"

    /** *****************************************************************************************************************/

    public static final def ERROR_MESSAGE = [
            101: '未找到交易对应的通道手续费设置',
            102: '未找到交易对应商户手续费设置',
            103: '原交易不存在',
            104: '原交易不允许该操作',
            105: '交易状态不允许该操作',
            106: '不支持的交易类型',
            107: '基础数据错误，无法后续逻辑'
    ]


}
