package com.cnepay.pos.clearing.fee.refund

import com.cnepay.pos.clearing.fee.sale.MerchantSaleCalculator
import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 通用退款计算器
 * User: andongxu
 * Time: 15-8-26 下午3:44 
 */
@Component
class CommonRefundCalculator extends RefundCalculator {

    Logger log = LoggerFactory.getLogger(CommonRefundCalculator)

    @Autowired
    Dao dao
    @Autowired
    MerchantSaleCalculator merchantSaleCalculator

    @Override
    def calcFee(transCurrent) {
        def fee = [:]
        def feeSettings = merchantSaleCalculator.getFeeSettings(transCurrent)
        def merchantFee = merchantSaleCalculator.calcFee(feeSettings, transCurrent)
        //实时清分不处理退货交易通道手续费
        fee.acqMerchantTransFee = null
        fee.acqTransFee = null
        fee.brandServiceFee = null
        fee.acqTransFeeRate = null
        fee.brandServiceFeeRate = null
        fee.merchantTransFee = merchantFee.merchantTransFee
        fee.merchantAddtionalFee = merchantFee.merchantAddtionalFee
        fee.merchantTransFeeRate = merchantFee.merchantTransFeeRate
        fee.merchantAddtionalFeeRate = merchantFee.merchantAddtionalFeeRate
        return fee
    }
}
