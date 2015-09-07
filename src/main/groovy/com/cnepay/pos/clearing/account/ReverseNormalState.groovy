package com.cnepay.pos.clearing.account

import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created with IntelliJ IDEA.
 * User: andongxu
 * Date: 15-9-7
 * Time: 下午11:35
 * To change this template use File | Settings | File Templates.
 */
class ReverseNormalState implements IAccountState {


    @Autowired
    IAccountState reverseUnfreezeState


    @Override
    def handle(AccountContext context) {




        context.setAccountState(reverseUnfreezeState)
    }
}
