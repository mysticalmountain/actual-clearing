package com.cnepay.pos.clearing.fee.sale

import com.cnepay.pos.clearing.util.FeeCalcullateException
import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 通道手续费计算器
 * User: andongxu
 * Time: 15-8-21 下午10:54 
 */
@Component
class AcquirerSaleCalculator extends SaleCalculator {

    Logger log = LoggerFactory.getLogger(AcquirerSaleCalculator)

    @Autowired
    Dao dao

    @Override
    def getFeeSettings(transCurrent) {
        def cardbin = dao.queryCardBin(String.valueOf(transCurrent.cardbin_id))
        def feeMode = this.getAcquirerFeeMode(cardbin, transCurrent)
        def feeSettings = dao.queryAcquirerFeeSettings(transCurrent.acquirer_id as String, feeMode as String)
        if (!feeSettings) {
            feeSettings = dao.queryAcquirerFeeSettings(transCurrent.acquirer_id as String, 0 as String)
        }
        if (!feeSettings) {
            def error = "未找到交易对应的通道手续费设置，交易ID:${transCurrent.id}, 商户号: ${transCurrent.merchant_no}, 通道ID:${transCurrent.acquirer_id}"
            log.error(error)
            //手续费计算失败，未找到通道手续费设置
            throw new FeeCalcullateException(null, error)
        }
        return feeSettings
    }

    @Override
    def calcFee(feeSettings, transCurrent) {
        def acquirerFee = [:]
        def trans_fee = 0;
        def acqTransFeeRate
        if (feeSettings.rate_type == Constants.FEE_SETTINGS_RATE) {             //按交易比例计算
            trans_fee = transCurrent.amount * feeSettings.params_a / 100
            acqTransFeeRate = feeSettings.params_a  + "%"
        } else if (feeSettings.rate_type == Constants.FEE_SETTINGS_FIXED) {     //按固定值计算
            trans_fee = feeSettings.params_b * 100
            acqTransFeeRate = feeSettings.params_b + "元/笔"
        } else if (feeSettings.rate_type == Constants.FEE_SETTINGS_CAP) {       //封顶
            trans_fee = transCurrent.amount * feeSettings.params_a / 100
            if (trans_fee > feeSettings.max_fee * 100) {
                trans_fee = feeSettings.max_fee * 100;
            }
            acqTransFeeRate = feeSettings.params_a + "%<" + feeSettings.min_x + "<" + feeSettings.max_fee
        }
        acquirerFee.acqMerchantTransFee =  BigDecimal.valueOf(trans_fee)
        acquirerFee.acqTransFee = BigDecimal.valueOf(trans_fee)
        acquirerFee.brandServiceFee = BigDecimal.valueOf(0)
        acquirerFee.acqTransFeeRate = acqTransFeeRate as String
        acquirerFee.brandServiceFeeRate = ""
        return acquirerFee
    }

    private def getAcquirerFeeMode(def cardbin, def transCurrent) {
        //默认使用全部卡BIN统一计费模式
        def acq_cardbin_type = 0
        //获取卡BIN
        if (null != cardbin) {
            if (cardbin.card_type == 'credit' || cardbin.card_type == 'semiCredit') {
                if (cardbin.issuer_no[0..3] == transCurrent.issuer_no) {
                    acq_cardbin_type = 2
                } else {
                    acq_cardbin_type = 4
                }
            } else {
                if (cardbin.issuer_no[0..3] == transCurrent.issuer_no) {
                    acq_cardbin_type = 1
                } else {
                    acq_cardbin_type = 3
                }
            }
        }
        return acq_cardbin_type
    }
}
