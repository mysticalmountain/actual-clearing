package com.cnepay.pos.clearing.fee.other

import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 通用反向交易计算器
 * 使用交易类型：消费冲正、消费撤销、消费撤销冲正；预授权完成冲正、预授权完成撤销、预授权完成撤销冲正
 * User: andongxu
 * Time: 15-8-26 下午1:10 
 */
@Component
class CommonReverseCalculator extends ReverseCalculator {

    Logger log = LoggerFactory.getLogger(CommonReverseCalculator)

    @Autowired
    Dao dao

    @Override
    def calcFee(transCurrent) {
        def fee = [:]
        def srcTrans = dao.querySrcTransCurrent(transCurrent.id)
        if (!srcTrans) {
            srcTrans = dao.querySrcTransHistory(transCurrent.id)
        }
        fee.acqMerchantTransFee = BigDecimal.valueOf(srcTrans.acq_merchant_trans_fee)
        fee.acqTransFee = BigDecimal.valueOf(srcTrans.acq_trans_fee)
        fee.brandServiceFee = srcTrans.brand_service_fee ? BigDecimal.valueOf(srcTrans.brand_service_fee) : null
        fee.acqTransFeeRate = srcTrans.acq_trans_fee_rate
        fee.brandServiceFeeRate = srcTrans.brand_service_fee_rate
        fee.merchantTransFee = BigDecimal.valueOf(srcTrans.merchant_trans_fee)
        fee.merchantAddtionalFee = BigDecimal.valueOf(srcTrans.merchant_addtional_fee)
        fee.merchantTransFeeRate = srcTrans.merchant_trans_fee_rate
        fee.merchantAddtionalFeeRate = srcTrans.merchant_addtional_fee_rate
        return fee
    }
}
