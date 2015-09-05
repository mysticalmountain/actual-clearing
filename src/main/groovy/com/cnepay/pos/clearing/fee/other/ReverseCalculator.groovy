package com.cnepay.pos.clearing.fee.other

import com.cnepay.pos.clearing.fee.ICalculator

/**
 * 反向交易计算器
 * User: andongxu
 * Time: 15-8-25 下午4:54 
 */
abstract class ReverseCalculator implements ICalculator{
    @Override
    def calculate(transCurrent) {
        return calcFee(transCurrent)
    }

    abstract def calcFee(transCurrent)
}
