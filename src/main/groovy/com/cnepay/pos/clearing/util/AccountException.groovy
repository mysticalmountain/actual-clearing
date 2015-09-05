package com.cnepay.pos.clearing.util

/**
 * 记账异常
 * User: andongxu
 * Time: 15-8-31 上午10:39 
 */
class AccountException extends Exception {

    String code
    String msg
    Throwable cause

    public AccountException(String code){
        this.code = code
    }

    public AccountException(String code, String msg){
        super(msg)
        this.code = code
        this.msg = msg
    }

    public AccountException(String code, String msg, Throwable cause){
        super(msg, cause)
        this.code = code
        this.msg = msg
        this.cause = cause
    }

    public AccountException(String msg, Throwable cause){
        super(msg, cause)
        this.msg = msg
        this.cause = cause
    }

    public AccountException(Throwable cause){
        super(cause)
    }
}
