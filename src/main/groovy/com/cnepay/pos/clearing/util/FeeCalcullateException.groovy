package com.cnepay.pos.clearing.util

/**
 * 手续费计算异常
 * User: andongxu
 * Time: 15-8-25 下午6:51 
 */
class FeeCalcullateException extends Exception{

    String code
    String msg
    Throwable cause

    public FeeCalcullateException(String code){
        this.code = code
    }

    public FeeCalcullateException(String code, String msg){
        super(msg)
        this.code = code
        this.msg = msg
    }

    public FeeCalcullateException(String code, String msg, Throwable cause){
        super(msg, cause)
        this.code = code
        this.msg = msg
        this.cause = cause
    }

    public FeeCalcullateException(String msg, Throwable cause){
        super(msg, cause)
        this.msg = msg
        this.cause = cause
    }

    public FeeCalcullateException(Throwable cause){
        super(cause)
    }
}
