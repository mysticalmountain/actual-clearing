package com.cnepay.pos.clearing.fee.sale

import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import com.cnepay.pos.clearing.util.FeeCalcullateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 商户手续费计算器
 * User: andongxu
 * Time: 15-8-21 下午11:00 
 */
@Component
class MerchantSaleCalculator extends SaleCalculator {

    Logger log = LoggerFactory.getLogger(MerchantSaleCalculator)

    @Autowired
    Dao dao

    @Override
    def getFeeSettings(transCurrent) {
        def cardBin = dao.queryCardBin(transCurrent.cardbin_id as String)
        def feeMode = this.getAcquirerFeeMode(cardBin)
        def feeSettings = dao.queryMerchantFeeSettings(transCurrent.merchant_no as String, feeMode as String)
        if (!feeSettings) {
            feeSettings = dao.queryMerchantFeeSettings(transCurrent.merchant_no as String, 0 as String)
        } else {
            log.error("未找到交易对应的商户手续费设置，交易ID:${transCurrent.id}, 商户号: ${transCurrent.merchant_no}, 通道ID:${transCurrent.acquirer_id}")
            //手续费计算失败，未找到通道手续费设置
            throw new FeeCalcullateException(102, Constants.ERROR_MESSAGE[102])
        }
    }

    @Override
    def calcFee(feeSettings, transCurrent) {
        def merchantFee = [:]
        def baseFeeSettings
        def additionalFeeSettings
        feeSettings.each { it ->
            if ("0".equals(it.fee_type as String)) {              //基本手续费
                baseFeeSettings = it
            } else if ("1".equals(it.fee_type)) {       //附加手续费
                additionalFeeSettings = it
            }
        }
        if (!baseFeeSettings) {
            log.error("未找到交易对应的商户基本手续费设置，交易ID:${transCurrent.id}, 商户号: ${transCurrent.merchant_no}, 通道ID:${transCurrent.acquirer_id}")
            throw new FeeCalcullateException(102, Constants.ERROR_MESSAGE[102])
        }
        //商户手续费计算。如果商户手续费为空，或者等于0计算手续费
        if (!transCurrent.merchant_trans_fee && BigDecimal.valueOf(0).compareTo(BigDecimal.valueOf(transCurrent.merchant_trans_fee)) == 0 ) {
            def merchantTransFee = this.calc(baseFeeSettings, transCurrent.amount)
            merchantFee.merchantTransFee = BigDecimal.valueOf(merchantTransFee.transFee)
            merchantFee.merchantTransFeeRate = merchantTransFee.transFeeRate
        }else{                          //使用已经计算的手续费
            merchantFee.merchantTransFee = BigDecimal.valueOf(0)
            merchantFee.merchantTransFeeRate = ""
        }
        //附加手续费计算
        def merchantAdditionalFee
        if (additionalFeeSettings) {
            merchantAdditionalFee = this.calc(baseFeeSettings, transCurrent.amount)
            merchantFee.merchantAdditionalFee = merchantAdditionalFee.transFee ? BigDecimal.valueOf(merchantAdditionalFee.transFee) : BigDecimal.valueOf(0)
            merchantFee.merchantAdditionalFeeRate = merchantAdditionalFee.transFeeRate ? merchantAdditionalFee.transFeeRate : ""
        }else{
            merchantFee.merchantAdditionalFee = BigDecimal.valueOf(0)
            merchantFee.merchantAdditionalFeeRate = ""
        }
        return merchantFee
    }

    /**
     * 获取卡BIN类型
     * @param cardbin
     * @return
     */
    private def getAcquirerFeeMode(def cardbin) {
        //默认使用全部卡BIN统一计费模式
        def cardbin_type = 0
        //获取卡BIN
        if (null != cardbin) {
            if (cardbin.card_type == 'credit' || cardbin.card_type == 'semiCredit') {
                cardbin_type = 2
            } else {
                cardbin_type = 1
            }
        }
    }

    /**
     * 计算手续费
     * @param feeSettings
     * @param amount
     * @return
     */
    private calc(def feeSettings, def amount) {
        def feeAndRate = [:]
        def transFee = 0
        def transFeeRate
        if (feeSettings.rate_type == Constants.FEE_SETTINGS_RATE) {                      //按交易比例计算
            transFee = amount * feeSettings.params_a / 100
            transFeeRate = feeSettings.params_a  + "%"
        } else if (feeSettings.rate_type == Constants.FEE_SETTINGS_FIXED) {               //按固定值计算
            transFee = feeSettings.params_b * 100
            transFeeRate = feeSettings.params_b + "元/笔"
        } else if (feeSettings.rate_type == Constants.FEE_SETTINGS_CAP) {               //封顶
            transFee = amount * feeSettings.params_a / 100
            if (transFee > feeSettings.max_fee * 100) {
                transFee = feeSettings.max_fee * 100
            }
            transFeeRate = feeSettings.params_a + "%<" + feeSettings.min_x + "<" + feeSettings.max_fee
        }
        feeAndRate.transFee = transFee
        feeAndRate.transFeeRate = transFeeRate
        return feeAndRate
    }
}
