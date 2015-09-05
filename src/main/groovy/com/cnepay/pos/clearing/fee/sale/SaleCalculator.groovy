package com.cnepay.pos.clearing.fee.sale

import com.cnepay.pos.clearing.fee.ICalculator
import com.cnepay.pos.clearing.util.Dao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import com.cnepay.pos.clearing.util.Constants
import org.springframework.stereotype.Component

/**
 * 消费手续费计算器
 * User: andongxu
 * Time: 15-8-21 下午10:45 
 */
abstract class SaleCalculator implements ICalculator {

    Logger log = LoggerFactory.getLogger(SaleCalculator)

    @Override
    def calculate(def transCurrent) {
        def feeSettings = getFeeSettings(transCurrent)
        def fee = calcFee(feeSettings, transCurrent)
        return fee
    }
    /**
     * 查询手续费设置
     * @param transCurrent
     * @return
     */
    abstract def getFeeSettings(def transCurrent)
    /**
     * 计算手续费
     * @param feeSettings
     * @param transCurrent
     * @return
     */
    abstract def calcFee(def feeSettings, def transCurrent)
}

