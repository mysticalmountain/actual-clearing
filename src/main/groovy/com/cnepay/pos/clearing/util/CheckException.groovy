package com.cnepay.pos.clearing.util

/**
 * 交易检查异常
 * User: andongxu
 * Time: 15-9-2 上午9:39 
 */
class CheckException extends Exception {

    String code
    String msg
    Throwable cause

    public CheckException(String code){
        this.code = code
    }

    public CheckException(String code, String msg){
        super(msg)
        this.code = code
        this.msg = msg
    }

    public CheckException(String code, String msg, Throwable cause){
        super(msg, cause)
        this.code = code
        this.msg = msg
        this.cause = cause
    }

    public CheckException(String msg, Throwable cause){
        super(msg, cause)
        this.msg = msg
        this.cause = cause
    }

    public CheckException(Throwable cause){
        super(cause)
    }

}
