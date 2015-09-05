package com.cnepay.pos.clearing.check

/**
 * 交易检查
 * User: andongxu
 * Time: 15-9-2 上午9:21 
 */
interface ICheck {

    def execute(def transCurrent)

}
