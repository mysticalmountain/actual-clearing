package com.cnepay.pos.clearing.fee.refund

import com.cnepay.pos.clearing.fee.ICalculator

/**
 * 退款计算器
 * User: andongxu
 * Time: 15-8-25 下午4:53 
 */
abstract class RefundCalculator implements ICalculator {

    @Override
    def calculate(transCurrent) {
        calcFee(transCurrent)
    }


    abstract def calcFee(transCurrent)
}
