package com.cnepay.pos.clearing.account

/**
 * Created with IntelliJ IDEA.
 * User: andongxu
 * Date: 15-9-7
 * Time: 下午11:20
 * To change this template use File | Settings | File Templates.
 */
interface IAccountState {


    def handle(AccountContext context)


}
