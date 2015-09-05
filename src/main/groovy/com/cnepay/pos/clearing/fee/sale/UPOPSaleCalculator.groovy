package com.cnepay.pos.clearing.fee.sale

import com.cnepay.pos.clearing.util.FeeCalcullateException
import com.cnepay.pos.clearing.util.Constants
import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 银联通道手续费计算器
 * User: andongxu
 * Time: 15-8-21 下午11:10 
 */
@Component
class UPOPSaleCalculator extends SaleCalculator {

    Logger log = LoggerFactory.getLogger(UPOPSaleCalculator)

    @Autowired
    Dao dao

    @Override
    def getFeeSettings(transCurrent) {
        //计费标识，（默认）00-标准计费， 04-县乡优惠
        def specialBillingType = (transCurrent.special_billing_type == 1) ? "04" : "00"
        def cardBin = dao.queryCardBin(transCurrent.cardbin_id as String)
        //跨境标识，默认0-境内卡，1-境外卡
        def foreigncardType = "05".equals(transCurrent.posp_no as String) ? "1" : "0"
        //获取卡BIN
        def cardType = getCardType(cardBin)
        def acq = dao.queryAcqIssuerNo(transCurrent.acq_merchant_no as String)
        //行业分类
        def mccType = getMccType(acq.mcc_type1, acq.mcc_type2)
        //查询银联通道费率
        def acquirerCost = dao.queryAcquirerCost(transCurrent.acquirer_id as String, specialBillingType, mccType, foreigncardType, cardType as String)
        if (acquirerCost) {
            return acquirerCost
        } else {
            String error = "未找到交易对应的通道手续费设置，交易ID:${transCurrent.transId}, 商户号: ${transCurrent.merchant_no}, 通道ID:${transCurrent.acquirer_id}"
            log.error(error)
            //手续费计算失败，未找到通道手续费设置
            throw new FeeCalcullateException(null, error)
        }
    }

    @Override
    def calcFee(feeSettings, transCurrent) {
        def acquirerFee = [:]
        def acqMerchantFeeRate
        def acqTransFee
        def brandServiceRate
        def brandServiceFee
        def acqMerchantTransFee
        //计算银行手续费
        def acqTransFeeTmp = transCurrent.amount * (feeSettings.acquirer_poundage_rates ?: 0) / 100
        def acqTransFeeMax = (feeSettings.acquirer_poundage_expenses ?: 0) * 100
        if (acqTransFeeTmp > acqTransFeeMax && acqTransFeeMax > 0) {
            acqMerchantFeeRate = "{" + (feeSettings.acquirer_poundage_expenses ?: 0) + "}元/笔"
            acqTransFee = acqTransFeeMax
        } else {
            acqMerchantFeeRate = feeSettings.acquirer_poundage_rates + "%"
            acqTransFee = acqTransFeeTmp
        }
        //计算品牌手续费
        def brandServiceFeeTmp = transCurrent.amount * (feeSettings.brand_service_rates ?: 0) / 100
        def brandServiceFeeMax = (feeSettings.brand_service_expenses ?: 0) * 100
        if (brandServiceFeeTmp > brandServiceFeeMax && brandServiceFeeMax > 0) {
            brandServiceRate = "{" + (feeSettings.brand_service_expenses ?: 0) + "}元/笔"
            brandServiceFee = brandServiceFeeMax
        } else {
            brandServiceRate = (feeSettings.brand_service_rates ?: 0) + "%"
            brandServiceFee = brandServiceFeeTmp
        }
        acqMerchantTransFee = new BigDecimal((acqTransFee + brandServiceFee).toString()).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()
        acquirerFee.acqMerchantTransFee = BigDecimal.valueOf(acqMerchantTransFee)
        acquirerFee.acqTransFee = BigDecimal.valueOf(acqTransFee)
        acquirerFee.brandServiceFee = BigDecimal.valueOf(brandServiceFee)
        acquirerFee.acqTransFeeRate = acqMerchantFeeRate
        acquirerFee.brandServiceFeeRate = brandServiceRate
        return acquirerFee
    }

    def getCardType(def cardBin) {
        //默认使用全部卡BIN统一计费模式
        def cardType = "debit"
        def cardBinType = 0
        if (null != cardBin) {
            if (cardBin.card_type == 'credit' || cardBin.card_type == 'semiCredit') {
                cardBinType = 2
            } else {
                cardBinType = 1
            }
            //卡类型 debit.借记卡（默认） dredit.贷记卡
            cardType = cardBinType == 2 ? "credit" : "debit"
        }
        return cardType
    }

    def getMccType(def mccType1, def mccType2) {
        def mccType
        if (mccType1 == "县乡优惠") {
            if (mccType2.indexOf("餐饮娱乐") > -1 || mccType2.indexOf("餐娱") > -1) {
                mccType = "餐娱类"
            } else if (mccType2.indexOf("三农") > -1) {
                mccType = "三农类"
            } else if (mccType2.indexOf("一般") > -1) {
                mccType = "一般类"
            } else if (mccType2.indexOf("房产汽车") > -1) {
                mccType = "房产汽车类"
            } else if (mccType2.indexOf("批发") > -1) {
                mccType = "批发类"
            } else {
                mccType = "民生类"
            }
        } else {
            if (mccType2.indexOf("批发") > -1) {
                mccType = "批发类"
            } else if (mccType2.indexOf("汽车销售") > -1 || mccType2.indexOf("房地产") > -1) {
                mccType = "房产汽车类"
            } else {
                mccType = mccType1
            }
        }
        return mccType
    }

}
