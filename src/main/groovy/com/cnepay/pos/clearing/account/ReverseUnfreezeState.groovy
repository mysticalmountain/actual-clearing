package com.cnepay.pos.clearing.account

import org.springframework.beans.factory.annotation.Autowired

/**
 * Created with IntelliJ IDEA.
 * User: andongxu
 * Date: 15-9-7
 * Time: 下午11:37
 * To change this template use File | Settings | File Templates.
 */
class ReverseUnfreezeState implements IAccountState {


    @Autowired
    IAccountState reverseAccountFailState


    @Override
    def handle(AccountContext context) {


        //有后续操作，设置新转台对象
        context.setAccountState(reverseAccountFailState)

    }
}
