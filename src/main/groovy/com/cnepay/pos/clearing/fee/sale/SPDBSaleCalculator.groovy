package com.cnepay.pos.clearing.fee.sale

import com.cnepay.pos.clearing.fee.ICalculator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * 北京浦发银行通道手续费计算器
 * User: andongxu
 * Time: 15-8-21 下午10:58 
 */
@Component
class SPDBSaleCalculator extends SaleCalculator{

    @Autowired
    AcquirerSaleCalculator saleCalculator

    @Override
    def getFeeSettings(transCurrent) {
        saleCalculator.getFeeSettings(transCurrent)
    }

    @Override
    def calcFee(feeSettings, amount) {
        def acquirerFee = saleCalculator.calculate(feeSettings, amount)
        def mixValue = BigDecimal.valueOf(1)
        def res = mixValue.compareTo(acquirerFee.acqMerchantTransFee)
        if (res == 1) {
            acquirerFee.acqMerchantTransFee = mixValue
            acquirerFee.acqTransFee = mixValue
        }
        return acquirerFee
    }
}
